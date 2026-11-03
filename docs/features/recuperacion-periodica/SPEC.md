# Recuperación ante notificaciones perdidas

Issue #47. Milestone: Persistencia y sincronización del catálogo.

## Primer incremento

`PeriodicCatalogSynchronization` ejecuta una sola comprobación mediante el caso de uso `SynchronizeCatalog`. Reutiliza la decisión de versiones, la aplicación incremental y la recuperación por snapshot; no introduce nuevas escrituras ni transacciones alrededor de llamadas externas.

Una invocación simultánea sobre la misma instancia se omite mientras la anterior esté en curso. El control se libera también ante fallos. Este control es local a la instancia: no es un bloqueo distribuido ni excluye el procesamiento de Kafka. La persistencia existente conserva su bloqueo y validación de versiones para las operaciones concurrentes.

RETRY_REQUIRED y LOCAL_VERSION_AHEAD se informan sin un bucle de reintentos. Los fallos de ejecución generan un mensaje fijo sin causas ni datos remotos; una invocación posterior puede volver a comprobar. La tarea no revierte cambios ya confirmados por el caso de uso ni modifica su atomicidad.

En este primer incremento la tarea no está registrada como bean ni tiene temporizador: no modifica el arranque de la aplicación. El siguiente incremento agregará activación explícita, intervalo configurable, ejecución sin solapamiento desde el fin de la comprobación anterior y cierre del planificador con la aplicación.

## Validación

Pruebas de todos los resultados, ausencia de reintentos inmediatos, recuperación tras un fallo, protección de información en logs y concurrencia entre invocaciones. No requiere conexión con cátedra.
