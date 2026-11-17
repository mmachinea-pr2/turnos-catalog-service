# Resultado del último intento de sincronización

Issue #57. Se amplía `GET /api/catalog/synchronization/status`, protegido con
JWT y ROLE_USER. Se conservan `initialized`, `appliedVersion` y `appliedAt`;
se agrega el objeto `lastAttempt`:

```json
{
  "initialized": true,
  "appliedVersion": 7,
  "appliedAt": "2026-11-11T12:00:00Z",
  "lastAttempt": {
    "startedAt": "2026-11-11T12:01:00Z",
    "completedAt": "2026-11-11T12:01:10Z",
    "result": "FAILED",
    "errorCode": "SYNCHRONIZATION_FAILED"
  }
}
```

La versión aplicada sigue representando exclusivamente los datos almacenados.
Un error posterior no borra ni adelanta esa versión. El resultado del intento
no garantiza conectividad actual ni disponibilidad de reservas.

## Estados

- `NEVER`: no hay intentos registrados desde esta migración; ambas fechas y el
  código de error son null, incluso si existe un catálogo cargado previamente.
- `RUNNING`: se persistió el comienzo; completedAt y errorCode son null.
- Resultados normales del coordinador: `INCREMENT_APPLIED`, `ALREADY_APPLIED`,
  `UP_TO_DATE`, `SNAPSHOT_APPLIED`, `SNAPSHOT_ALREADY_COVERED`.
- `RETRY_REQUIRED` y `LOCAL_VERSION_AHEAD`: resultados que requieren otra
  comprobación o revisión; no se presentan como sincronización exitosa ni
  excepciones. El resultado conserva su significado y errorCode es null.
- `FAILED`: excepción en la operación; código público fijo
  `SYNCHRONIZATION_FAILED`. No se persisten mensajes de excepciones, respuestas
  externas, URLs ni credenciales.

El inicio de un nuevo intento limpia el resultado anterior. El documento expone
el último intento, no un historial de errores ni la última falla histórica.

## Persistencia y concurrencia

Flyway V3 crea `catalog_sync_attempt` con una fila. Inicio y fin se guardan en
transacciones cortas REQUIRES_NEW, fuera de la transacción que aplica los datos.
Así, un rollback de la operación no elimina la evidencia del fallo. No se
mantiene una transacción abierta durante llamadas de red por este registro.

Cada inicio reemplaza el identificador de intento. El fin actualiza solamente
si ese identificador sigue siendo el vigente, para impedir que un intento
anterior sobrescriba otro iniciado después. Este control no modifica los bloqueos
y controles de versión del catálogo. No serializa las llamadas remotas.

El coordinador usado por Kafka, recuperación periódica y ejecución manual queda
instrumentado. La carga manual directa de snapshot también. Si el coordinador
invoca un snapshot como recuperación, ambos pertenecen a un único intento;
la protección de anidamiento es por hilo y se libera incluso si ocurre un error.
Notificaciones duplicadas que no requieren sincronizar no crean un intento.

Si el proceso termina abruptamente o no puede persistir el final, puede quedar
`RUNNING` aun después de reiniciar. Significa «inicio sin final registrado», no
prueba que todavía se esté ejecutando. La próxima sincronización reemplaza ese
registro. Si no puede registrarse el inicio, no se ejecuta la operación; si falla
la persistencia del final, no se inventa un resultado exitoso. La versión aplicada
puede haber avanzado antes del fallo al registrar el final.

## Compatibilidad y pruebas

Booking ignora campos desconocidos al leer el estado, por lo que puede consumir
la respuesta ampliada. No se modifica el contrato de agendas ni la autorización.

Pruebas: resultados del coordinador, error con código seguro, recuperación,
anidamiento sin doble registro, fallos al registrar, migración MySQL, persistencia
independiente de rollback, lectura HTTP y finalización atrasada sin regresión.
Ejecutar `./mvnw -B -ntp verify` con Java 25 y Docker.
