# Coordinar la lectura y aplicación de un incremento

El caso de uso interno `SynchronizeCatalogIncrement.synchronize()` conecta los lectores de estado, versiones, cambios y entidades con `CatalogIncrementStore`. Se registra cuando `CATEDRA_REDIS_ENABLED=true`; no se ejecuta al iniciar la aplicación.

## Flujo

1. Leer la versión local y las versiones remotas.
2. Si falta inicialización o la versión local quedó fuera de la ventana, indicar SNAPSHOT_REQUIRED.
3. Si la versión local coincide con la remota, devolver UP_TO_DATE. Si la supera, devolver LOCAL_VERSION_AHEAD, sin retroceder.
4. Exigir que falte exactamente una versión. Si faltan varias, indicar SNAPSHOT_REQUIRED: los Hashes vigentes no permiten reconstruir cada estado histórico intermedio.
5. Obtener los IDs de esa versión. Una clave ausente requiere snapshot; un conjunto vacío válido puede aplicarse.
6. Leer las entidades y verificar que la versión y los IDs de cada colección coincidan exactamente con lo solicitado.
7. Volver a consultar las versiones remotas. Si cambió la versión actual o el límite de retención, descartar el lote y devolver RETRY_REQUIRED.
8. Invocar el guardado transaccional: APPLIED si se guardó, ALREADY_APPLIED si otro escritor ya cubrió la versión.

Las lecturas externas ocurren antes de la transacción de persistencia. Un error de cualquier lector o de persistencia se propaga sin convertirse en éxito; no hay reintentos automáticos ni escrituras parciales del coordinador. El adaptador de persistencia conserva el bloqueo y la comprobación de versiones.

## Decisiones y límites

La decisión general existente puede indicar que hay incrementos retenidos para varias versiones. Este coordinador adopta una estrategia conservadora y solicita snapshot en ese caso, porque los valores actuales de los Hashes no son una historia de cada versión.

SNAPSHOT_REQUIRED solo informa la necesidad; todavía no descarga ni aplica el snapshot. RETRY_REQUIRED tampoco inicia otro intento. No hay endpoint, tarea programada ni consumidor Kafka nuevo.

Comparar versiones antes y después detecta cambios en los metadatos publicados, pero no constituye una transacción distribuida ni garantiza que el publicador actualice entidades y versión atómicamente. Esta comprobación no demuestra ausencia de una publicación en curso que todavía no modificó la versión. El servicio trabaja con el contrato de versiones publicado por la cátedra; no afirma representar versiones históricas.

## Pruebas

Estados inicial, actual y adelantado; ventana agotada; varias versiones pendientes; cambios ausentes o vacíos; lote incompleto o de otra versión; metadatos que cambian; fallos en cada lectura; duplicado concurrente y rechazo del guardado. Prueba con MySQL temporal del flujo completo con lectores simulados y persistencia real, verificando que las lecturas externas ocurran sin transacción abierta.
