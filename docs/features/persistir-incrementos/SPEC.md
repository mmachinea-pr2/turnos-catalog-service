# Persistir un incremento del catálogo

El puerto `CatalogIncrementStore.apply(CatalogChangedEntities)` guarda un lote ya obtenido y validado, sin realizar llamadas externas. Devuelve true si lo aplica y false si la versión ya estaba cubierta localmente.

## Reglas

- Debe existir el estado de sincronización y una versión inicial aplicada mediante snapshot.
- Una versión igual o anterior se ignora sin modificar datos ni appliedAt.
- Una versión nueva debe ser exactamente la siguiente a la local; los saltos producen CatalogIncrementRejectedException. El estado faltante o sin inicializar también produce ese error propio.
- Se insertan o actualizan categorías, luego profesionales y luego horarios, respetando las claves foráneas.
- Las entidades que no aparecen en el lote se conservan. Las bajas lógicas se guardan con enabled=false, sin eliminación física ni deshabilitación en cascada.
- Los IDs deben ser positivos y únicos dentro de cada colección. Las referencias pueden resolverse contra entidades locales o contra padres del propio lote.
- Un lote vacío válido avanza la versión; una versión remota ausente no equivale a un lote vacío.

## Transacción y concurrencia

El adaptador JPA bloquea la fila de estado con el mismo bloqueo pesimista utilizado por los snapshots. Guarda los datos y actualiza appliedVersion y appliedAt dentro de una única transacción. Cualquier error revierte también las escrituras previas del lote. Dos ejecuciones concurrentes de la misma versión producen una aplicación y una omisión.

No mantiene una transacción abierta mientras consulta Redis. Este puerto no consulta Redis ni REST: recibe exclusivamente datos en memoria.

## Límites

Todavía no se conecta automáticamente este guardado con los lectores Redis ni con Kafka. Antes de hacerlo, el coordinador deberá comprobar disponibilidad de todos los cambios y estabilidad de la versión remota. Los Hashes contienen entidades vigentes, no históricas: este adaptador por sí solo no demuestra que un lote leído externamente corresponda a la versión indicada. No debe utilizarse como ejecutor de sincronización hasta resolver esa comprobación.

No incorpora endpoints, tareas programadas ni nuevas migraciones. La sincronización manual por snapshot conserva su comportamiento de reemplazo completo.

## Pruebas

MySQL temporal: inserciones y actualizaciones, preservación de filas no modificadas, bajas lógicas, lote vacío, versiones repetidas y antiguas, estado inicial, saltos, estado faltante, rollback ante una referencia inválida y aplicación concurrente duplicada. Validación del modelo ante IDs duplicados o no positivos.
