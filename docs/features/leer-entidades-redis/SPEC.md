# Leer entidades modificadas desde Redis

El puerto `CatalogEntitiesSource.read(CatalogChanges)` devuelve `CatalogChangedEntities` con la versión solicitada y listas inmutables de categorías, profesionales y horarios.

El adaptador consulta por HMGET únicamente los IDs recibidos en los Hashes `catedra:sync:professional-categories`, `catedra:sync:professionals` y `catedra:sync:weekly-schedules`. Omite listas vacías y, si todo el lote está vacío, devuelve un resultado vacío sin conectarse. Conserva el orden solicitado.

Cada JSON debe contener el ID esperado, campos obligatorios del tipo correcto y fechas y horarios válidos. Las referencias deben ser IDs positivos; se validan la duración positiva y el orden de los horarios. Se admiten campos adicionales y descripción nula o ausente. No se convierten strings numéricos ni booleanos de texto.

Las entidades con enabled=false se conservan: representan bajas lógicas, según `INTEGRATION_REFERENCE-v2.md`, sección 14.4. Si un ID falta, el JSON es inválido o falla Redis, la lectura completa produce `CatalogEntitiesReadException`. No devuelve resultados parciales ni trata una entidad faltante como una baja. El mensaje es fijo y no incluye cuerpos remotos, credenciales ni la causa original.

## Alcance y consistencia

Los Hashes contienen el estado vigente, no una copia histórica de cada versión. La versión del resultado identifica el lote solicitado y no garantiza que los valores correspondan históricamente a ese instante. Las lecturas de los tres Hashes no son una operación atómica conjunta. El futuro ejecutor deberá comprobar la versión remota y resolver cambios concurrentes antes de guardar o avanzar el estado local.

Un lote puede contener profesionales cuyos padres no hayan cambiado, por lo que no se exige que todas las referencias estén dentro del mismo lote. La validación contra la copia local queda para la futura aplicación transaccional.

Este paso no escribe en Redis ni MySQL, no avanza la versión local, no agrega endpoints ni consume Kafka. Todavía no conecta automáticamente la lectura de IDs con la lectura de entidades. Reutiliza la configuración y el cliente Redis opcionales existentes; habilitarlo no consulta servidores durante el arranque.

## Pruebas

Redis temporal con ACL de solo lectura: los tres tipos de entidad, bajas lógicas, subconjuntos y orden, IDs ausentes o incorrectos, formatos inválidos, referencias inválidas, horarios inválidos, campos adicionales, documentos extra y fallos de autenticación. Los cambios vacíos no abren conexión.
