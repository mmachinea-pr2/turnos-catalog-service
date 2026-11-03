# Recuperación ante notificaciones perdidas

Issue #47. Milestone: Persistencia y sincronización del catálogo.

## Primer incremento

`PeriodicCatalogSynchronization` ejecuta una sola comprobación mediante el caso de uso `SynchronizeCatalog`. Reutiliza la decisión de versiones, la aplicación incremental y la recuperación por snapshot; no introduce nuevas escrituras ni transacciones alrededor de llamadas externas.

Una invocación simultánea sobre la misma instancia se omite mientras la anterior esté en curso. El control se libera también ante fallos. Este control es local a la instancia: no es un bloqueo distribuido ni excluye el procesamiento de Kafka. La persistencia existente conserva su bloqueo y validación de versiones para las operaciones concurrentes.

RETRY_REQUIRED y LOCAL_VERSION_AHEAD se informan sin un bucle de reintentos. Los fallos de ejecución generan un mensaje fijo sin causas ni datos remotos; una invocación posterior puede volver a comprobar. La tarea no revierte cambios ya confirmados por el caso de uso ni modifica su atomicidad.

## Temporizador opcional

El temporizador está desactivado por defecto. Cuando se habilita, comienza después de ApplicationReadyEvent, espera el retraso inicial y ejecuta una comprobación. Cada intervalo se cuenta desde el fin de la anterior, también si falla; no hay reintentos inmediatos ni tareas acumuladas. Cada comprobación reutiliza los timeouts REST y Redis existentes. Una base vacía puede inicializarse mediante el snapshot sin esperar un evento Kafka.

Variables externas:

| Variable | Valor por defecto | Límites |
| --- | --- | --- |
| CATALOG_PERIODIC_ENABLED | false | true para activar |
| CATALOG_PERIODIC_INTERVAL | 60s | 1 segundo a 1 día |
| CATALOG_PERIODIC_INITIAL_DELAY | 5s | 0 a 1 día |

Requiere CATEDRA_REST_ENABLED=true y CATEDRA_REDIS_ENABLED=true, además de las credenciales locales. Si faltan las dependencias o los intervalos son inválidos, el arranque falla. No admite ninguno de los modos manuales de sincronización activos simultáneamente. Los comandos manuales deben pasar `--catalog.synchronization.periodic.enabled=false` si el entorno activa el temporizador.

Al cerrar el contexto se cancelan las ejecuciones futuras y se solicita la interrupción del trabajo en curso. Una llamada externa que no responda a la interrupción puede finalizar por su timeout; no se promete cancelación instantánea ni se revierte una transacción ya confirmada. La seguridad transaccional sigue en la persistencia.

Con `.env` completo, Java 25 y MySQL disponible:

```bash
set -a
. ./.env
set +a
unset MYSQL_ROOT_PASSWORD
./mvnw spring-boot:run -Dspring-boot.run.arguments="--catalog.catedra.enabled=true --catalog.catedra.redis.enabled=true --catalog.synchronization.periodic.enabled=true"
```

Si 8080 está ocupado, agregar `--server.port=18081` dentro de los argumentos. Para Compose, configurar las tres variables de activación en `.env` y ejecutar `docker compose up --build -d`. Compose transmite las variables sin incorporarlas a la imagen. No ejecutar varias instancias de prueba contra la misma base sin necesidad.

La recuperación periódica no sustituye al consumidor Kafka: cuando se integren ambos PR, podrán ejecutarse juntos. No reinicia un consumidor detenido, no confirma offsets y no registra eventos como procesados. El indicador de Kafka seguirá reflejando su estado propio. Una recuperación por versiones no prueba por sí sola la recepción de una notificación.

## Validación

Pruebas de todos los resultados, ausencia de reintentos inmediatos, recuperación tras un fallo, protección de información en logs y concurrencia entre invocaciones. No requiere conexión con cátedra.
