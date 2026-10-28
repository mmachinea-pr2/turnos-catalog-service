# Caso de uso de sincronización por snapshot

## Objetivo

Unir la consulta externa del snapshot validado con su persistencia atómica mediante el puerto de entrada SynchronizeCatalogSnapshot.

## Comportamiento

1. Obtener y validar el snapshot mediante CatalogSnapshotSource.
2. Entregarlo a CatalogSnapshotStore, que compara la versión bajo bloqueo y confirma datos y versión juntos.
3. Devolver true cuando se aplicó una nueva versión o false si la versión recibida ya estaba cubierta por la copia local.

Si falla la consulta o la validación, no se invoca la persistencia. Si falla el guardado, su transacción revierte todos los cambios y el error se propaga. No hay reintentos automáticos.

## Arquitectura y límites

El servicio de aplicación usa Java puro y depende de los puertos. La consulta de red ocurre antes de abrir la transacción de persistencia; los futuros adaptadores de entrada deberán invocar el caso de uso fuera de una transacción. La configuración Spring conecta los adaptadores existentes cuando catalog.catedra.enabled=true.

El arranque no realiza consultas ni escrituras. Esta entrega no agrega endpoint, tarea programada ni escucha Kafka: prepara una operación interna que podrán invocar esos adaptadores en próximos incrementos. Tampoco confirma offsets Kafka ni aplica incrementos Redis.

## Validación

Las pruebas integran el cliente REST con respuestas simuladas y la persistencia real en MySQL temporal. Verifican la primera aplicación, repetición sin cambios, ausencia de transacción durante la consulta, conservación de la copia ante HTTP 503 y rollback de una escritura fallida. No requieren la red de la cátedra ni secretos reales.
