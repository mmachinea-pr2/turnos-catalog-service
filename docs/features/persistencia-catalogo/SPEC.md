# Persistencia del catálogo

## Problema y resultado esperado

La base inicial puede arrancar y conectarse a MySQL, pero aún necesita un esquema para conservar la copia local de categorías, profesionales y horarios de la cátedra. Se requiere almacenar esas entidades y la versión aplicada sin perder sus relaciones.

## Fuente y alcance

El diseño utiliza los campos de snapshot de INTEGRATION_REFERENCE-v2.md y la separación de datos exigida en PROJECT_STATEMENT-v1.md. El [modelo de datos](../../modelo-datos.md) define tablas, relaciones y reglas.

Se incluyen migración Flyway, entidades y repositorios de infraestructura, versión local y pruebas MySQL. Quedan fuera los clientes REST/Redis/Kafka, endpoints, autenticación, reservas y frontend.

## Criterios de aceptación

1. Conservar los IDs y los registros habilitados y deshabilitados de la cátedra.
2. Mantener integridad entre categorías, profesionales y horarios.
3. Guardar instantes de auditoría en UTC y horarios como horas locales.
4. Mantener versión y fecha en NULL antes de la primera sincronización; aceptar versión cero.
5. Permitir actualizar datos y versión en una única transacción que revierta ambos ante fallos.
6. Ejecutar migraciones y pruebas en MySQL real, sin depender de datos de desarrollo.

## Límites

La estructura de persistencia no constituye por sí sola la implementación de sincronización. Las entidades JPA pertenecen a infraestructura y no reemplazan los futuros modelos de dominio ni los DTO HTTP.
