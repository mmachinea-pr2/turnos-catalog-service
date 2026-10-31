# Determinar la acción de sincronización

El caso de uso interno `DetermineCatalogSynchronization.determine()` consulta el estado local mediante `CatalogSynchronizationStatusReader` y las versiones remotas mediante `CatalogVersionSource`.

| Condición | Resultado |
| --- | --- |
| Sin versión local aplicada | `FETCH_SNAPSHOT` |
| Versión local mayor que la actual remota | `LOCAL_VERSION_AHEAD` |
| Versión local igual a la actual remota | `UP_TO_DATE` |
| Versión local menor que la más antigua disponible | `FETCH_SNAPSHOT` |
| Versión local dentro de la ventana y menor que la actual | `APPLY_INCREMENTS` |

Se sigue la comparación estricta indicada en `INTEGRATION_REFERENCE-v2.md`, sección 14.2: una versión local igual a oldestAvailableVersion puede usar incrementos. Cero es una versión válida; null significa que nunca se aplicó un snapshot.

Una versión local adelantada se informa como anomalía sin ordenar un retroceso. Un estado local negativo produce error. Los fallos de lectura se propagan: no se sustituyen por un catálogo vacío ni por una confirmación de que está actualizado.

## Alcance

El servicio es Java puro y depende únicamente de puertos de lectura. La configuración Spring lo habilita con `CATEDRA_REDIS_ENABLED=true`; crear el bean no consulta los servidores. El cliente REST puede seguir desactivado porque este paso solo decide una acción.

No agrega endpoints, tareas programadas, escrituras ni ejecución automática de snapshots o incrementos. El endpoint existente de estado local conserva su contrato. La decisión representa las lecturas realizadas, no una garantía contra cambios concurrentes: el futuro ejecutor deberá volver a comprobar versiones y disponibilidad al aplicar cambios.

## Pruebas

Casos iniciales, cero, igualdad, límites de retención, versión adelantada, valores cercanos a Long.MAX_VALUE, estado negativo y errores de ambos lectores. Configuración opcional sin consultas durante el arranque.
