# Eventos de catálogo y consumidor Kafka

La funcionalidad se desarrolla en varios commits dentro de una issue. Este primer incremento prepara el contrato, el procesamiento y el registro durable de eventos. Todavía no abre una conexión Kafka ni confirma offsets.

## Contrato de entrada

`CatalogUpdatedEventDecoder` acepta el JSON de `CatalogUpdated`, schemaVersion=1 y una message key igual a newVersion en texto decimal. Exige eventId no vacío, versión no negativa, occurredAt válido y source no vacío. Ignora campos adicionales, pero rechaza tipos incorrectos, campos ausentes, versiones de esquema incompatibles, claves inconsistentes y documentos JSON adicionales. No convierte strings numéricos.

El límite técnico de eventId es 255 bytes UTF-8, acorde a la clave de almacenamiento local; un valor mayor se rechaza, nunca se trunca. La fuente es texto, para admitir nuevos orígenes sin cambiar el contrato. El error de decodificación no incluye el payload ni su causa.

## Procesamiento

`HandleCatalogUpdated` usa eventId como clave funcional de idempotencia. Un evento ya registrado se devuelve como DUPLICATE sin consultar servicios externos. Reutilizar un eventId para otra versión produce CatalogEventConflictException.

Si la versión local ya cubre el evento, lo registra sin sincronizar. En caso contrario ejecuta `SynchronizeCatalog` una vez y vuelve a consultar la versión local. Solo marca PROCESSED si esta cubre newVersion. Un resultado aparentemente exitoso de sincronización no basta: un evento puede llegar antes que los datos. En ese caso devuelve RETRY_REQUIRED sin registrar el evento. También conserva los resultados de reintento o versión adelantada y propaga los errores.

El evento notifica, no contiene entidades ni modifica directamente la versión local. Los datos siguen obteniéndose mediante Redis o REST.

## Persistencia

La migración nueva `V2__processed_catalog_event.sql` agrega una tabla propia con event_id, new_version y processed_at UTC. No modifica V1. La clave binaria conserva diferencias de mayúsculas y espacios del identificador.

El registro toma el mismo bloqueo de estado que los snapshots e incrementos, comprueba de nuevo la versión y evita filas duplicadas ante escrituras concurrentes. Solo esa comprobación y registro usan una transacción corta; las consultas externas quedan fuera.

Guardar datos y registrar el evento son operaciones separadas. Si el proceso cae entre ambas, la repetición encuentra la versión cubierta y puede registrar el evento sin volver a aplicar el cambio. Dos manejadores simultáneos podrían consultar servicios externos, pero la persistencia de catálogo conserva sus controles de versión y el registro produce una sola fila. No se promete ejecución exactamente una vez.

El registro no se borra durante snapshots. Aún no hay política de purga: eliminar registros afectaría la deduplicación por eventId.

## Siguiente incremento de la misma issue

Configurar el consumidor Kafka opcional para el topic y grupo propios, conectar el decodificador y el manejador, definir confirmaciones y manejo de fallos, y probar con un broker local. No se usarán reportes de otros alumnos como prueba de que el servidor de cátedra funciona o falla.

## Pruebas

Validación del mensaje y clave, campos adicionales, duplicados, eventos antiguos, eventos adelantados respecto de los datos y fallos sin registro. MySQL temporal: migración, persistencia, identidad sensible a mayúsculas, colisiones de versión, concurrencia y procesamiento con guardado real fuera de la transacción de red.
