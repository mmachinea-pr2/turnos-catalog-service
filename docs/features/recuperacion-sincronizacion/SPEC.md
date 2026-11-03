# Recuperación y ejecución de la sincronización

Esta funcionalidad se desarrolla en varios commits dentro de una misma issue.

## Primer incremento: recuperación por snapshot

El caso de uso `SynchronizeCatalog.synchronize()` realiza un único intento. Primero invoca el coordinador incremental. Si devuelve SNAPSHOT_REQUIRED, obtiene y aplica un snapshot mediante el caso de uso existente. Los otros resultados no descargan un snapshot.

| Resultado incremental | Resultado del flujo |
| --- | --- |
| APPLIED | INCREMENT_APPLIED |
| ALREADY_APPLIED | ALREADY_APPLIED |
| UP_TO_DATE | UP_TO_DATE |
| LOCAL_VERSION_AHEAD | LOCAL_VERSION_AHEAD |
| RETRY_REQUIRED | RETRY_REQUIRED |
| SNAPSHOT_REQUIRED y snapshot guardado | SNAPSHOT_APPLIED |
| SNAPSHOT_REQUIRED y versión ya cubierta localmente | SNAPSHOT_ALREADY_COVERED |

La recuperación cubre las situaciones detectadas por el coordinador: catálogo sin inicializar, retención insuficiente, cambios ausentes o varias versiones pendientes. Los errores de Redis o REST se propagan; no se interpretan silenciosamente como necesidad de recuperación. No hay bucles de reintento.

Las llamadas externas ocurren fuera de la transacción. El guardado del snapshot mantiene su bloqueo y su reemplazo atómico de datos y versión. Una descarga fallida deja intacto el estado local. SNAPSHOT_APPLIED informa que se guardó una versión, no garantiza que la cátedra no haya publicado otra posteriormente. SNAPSHOT_ALREADY_COVERED no implica por sí mismo igualdad con la última versión remota.

El servicio se registra cuando ambos clientes están habilitados (`CATEDRA_REST_ENABLED=true` y `CATEDRA_REDIS_ENABLED=true`). Su creación no inicia consultas ni escrituras. El comando de carga manual de snapshot existente conserva su comportamiento.

## Segundo incremento: ejecución manual explícita

El modo `catalog.synchronization.full-run-on-startup=true` ejecuta el caso de uso general una vez. Exige modo no web y ambos clientes configurados. No puede combinarse con `catalog.synchronization.run-on-startup=true`, que sigue siendo el modo de snapshot directo. Una configuración incompatible falla antes de consultar los servicios.

Desde la terminal del repositorio, con ZeroTier conectado y los datos propios de REST y Redis completos en `.env`:

```bash
sdk env
set -a
. ./.env
set +a
unset MYSQL_ROOT_PASSWORD
docker compose up -d --wait mysql
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.main.web-application-type=none --catalog.catedra.enabled=true --catalog.catedra.redis.enabled=true --catalog.synchronization.run-on-startup=false --catalog.synchronization.periodic.enabled=false --catalog.synchronization.full-run-on-startup=true"
```

Las credenciales no deben escribirse como argumentos ni guardarse en Git. Spring no lee `.env` automáticamente: el bloque anterior exporta las variables para el proceso.

El comando informa si aplicó un incremento, aplicó un snapshot, encontró la versión actual o si la versión ya estaba cubierta localmente. Al terminar correctamente, el método main cierra el contexto Spring y sus conexiones; no queda un servidor HTTP abierto. Los dos indicadores de ejecución están desactivados por defecto, por lo que el arranque habitual conserva su comportamiento.

Si los metadatos cambian durante la lectura (RETRY_REQUIRED), el comando falla indicando que se ejecute nuevamente más tarde. Si la versión local está adelantada (LOCAL_VERSION_AHEAD), falla para que se revise el estado remoto. No reintenta en bucle ni informa esos casos como éxito. Las excepciones de ejecución se sustituyen por un mensaje fijo sin encadenar sus causas. Un error produce salida fallida del comando; el guardado conserva sus garantías transaccionales.

No se incorpora un endpoint público, temporizador ni consumidor Kafka en esta issue.

## Pruebas del primer incremento

Selección de recuperación y propagación de los demás resultados; fallos sin reintentos; configuración condicionada a ambos clientes. Flujo con REST simulado y MySQL real temporal: se recupera un catálogo atrasado y se conserva el anterior ante un error HTTP. Las pruebas no consultan los servicios docentes.


## Pruebas del segundo incremento

Pruebas del runner para resultados, errores y configuración incompatible. Integración que invoca `CatalogApplication.main` con MySQL y Redis temporales, ACL de lectura y HTTP local simulado. Cubre snapshot inicial, versión actual, incremento, cambios ausentes con recuperación por snapshot y error HTTP conservando datos y versión. Cada ejecución exitosa cierra el contexto antes de retornar. No se ejecutó contra el servidor docente ni sobre la base de desarrollo.
