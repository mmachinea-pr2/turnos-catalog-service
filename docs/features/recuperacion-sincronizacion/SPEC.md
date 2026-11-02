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

## Segundo incremento previsto en esta misma issue

Agregar una ejecución manual explícita del flujo completo, con resultados legibles, manejo de errores y pruebas. Este modo todavía no está implementado. No se incorpora un endpoint público ni ejecución periódica en el primer commit.

## Pruebas del primer incremento

Selección de recuperación y propagación de los demás resultados; fallos sin reintentos; configuración condicionada a ambos clientes. Flujo con REST simulado y MySQL real temporal: se recupera un catálogo atrasado y se conserva el anterior ante un error HTTP. Las pruebas no consultan los servicios docentes.
