# Persistencia atómica del snapshot

## Objetivo

Guardar un CatalogSnapshot validado junto con su versión en una única transacción MySQL. Es un paso de la sincronización local; la obtención por REST y las notificaciones Kafka quedan para otra entrega.

## Comportamiento

- La primera carga acepta la versión cero.
- Un snapshot más reciente reemplaza las tres colecciones completas, incluyendo bajas lógicas y conservando IDs y fechas de origen.
- Los registros ausentes en el snapshot desaparecen de la copia local.
- Un snapshot vacío es válido y vacía el catálogo.
- Versiones iguales o anteriores no modifican datos ni fecha de aplicación; apply devuelve false. Esta es una decisión local de idempotencia por versión, no la futura deduplicación de eventId.
- Datos y versión se confirman juntos. Cualquier error de persistencia revierte el reemplazo.
- La fila única del estado se bloquea durante la transacción para serializar aplicaciones de snapshots. Si falta, la operación falla antes de borrar datos.
- appliedAt representa el instante local de aplicación, no generatedAt del origen.

## Arquitectura

CatalogSnapshotStore es un puerto de salida sin dependencias de Spring. JpaCatalogSnapshotStore lo implementa con repositorios JPA y delimita la transacción. Se borran horarios, profesionales y categorías en ese orden; se insertan en orden inverso para respetar las referencias.

Los futuros escritores de sincronización deberán respetar el mismo bloqueo. Todavía no se expone un endpoint ni se ejecuta sincronización automática.
