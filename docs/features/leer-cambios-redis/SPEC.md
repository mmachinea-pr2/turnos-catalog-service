# Leer los cambios de una versión desde Redis

`CatalogChangesSource.read(version)` consulta `catedra:sync:changes:{version}` con GET y devuelve un `Optional<CatalogChanges>`.

- Una clave ausente produce Optional.empty(): no debe interpretarse como una versión aplicada ni como una lista vacía.
- Un documento válido con las tres listas vacías produce un resultado presente, sin IDs afectados.
- La versión del documento debe coincidir con la solicitada y ser un entero no negativo.
- Las tres listas (`professionalCategories`, `professionals`, `weeklySchedules`) son obligatorias. Sus IDs deben ser enteros positivos dentro del rango long, sin duplicados dentro de cada lista. El mismo ID puede aparecer en tipos de entidad distintos.
- Se ignoran campos adicionales. Se rechazan JSON mal formado, documentos adicionales, números decimales, strings numéricos, nulos y desbordamientos.

`CatalogChanges` contiene copias inmutables de las listas y no depende de frameworks. El adaptador usa Lettuce y Jackson; no devuelve DTOs de infraestructura.

Los errores de Redis, autenticación, tipos incorrectos y contenido inválido producen `CatalogChangesReadException` con un mensaje fijo y sin causa que exponga credenciales o contenido remoto. Una versión negativa solicitada produce IllegalArgumentException antes de conectarse.

## Configuración y alcance

Reutiliza el cliente opcional de Redis, sus credenciales externas y tiempos límite. El bean se registra solamente con `CATEDRA_REDIS_ENABLED=true`; el arranque no realiza lecturas. No requiere variables adicionales.

Este paso solo obtiene los IDs afectados, según `INTEGRATION_REFERENCE-v2.md`, sección 14.5. No obtiene todavía las entidades desde sus Hashes, no modifica MySQL, no avanza la versión local y no conecta aún este lector al ejecutor de sincronización. La ausencia de una versión permitirá decidir una recuperación por snapshot en un avance posterior.

## Pruebas

Redis temporal con usuario de solo lectura: lectura de las tres listas, campos adicionales, versión ausente y vacía, JSON inválido, IDs inválidos, versión incorrecta, tipo Redis incorrecto y autenticación fallida. Pruebas unitarias de inmutabilidad y validación del modelo. No se consulta el servidor docente.
