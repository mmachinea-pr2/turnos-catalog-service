# Dominio del catálogo

## Objetivo

Separar los datos y reglas del catálogo de la representación JPA, para que los próximos casos de uso no dependan de entidades de base de datos.

## Alcance

Modelos inmutables ProfessionalCategory, Professional y WeeklySchedule en Java puro. Mapeo explícito entre modelos y entidades, ubicado en infraestructura. Pruebas unitarias sin Spring ni MySQL para reglas y conversiones.

## Criterios de aceptación

- El paquete domain solo importa clases del JDK.
- Los modelos conservan IDs externos, campos del contrato, estado habilitado y fechas.
- La descripción de categoría puede ser nula; referencias, nombres y fechas obligatorias no pueden ser nulos.
- Los horarios requieren duración positiva y fin posterior al inicio, como el esquema existente.
- No se exige que la duración total sea divisible por la duración del turno.
- Los mapeos conservan valores sin recortar nombres ni convertir horas locales.
- Un horario inválido no puede convertirse en un modelo de dominio válido.

## Límites

Los modelos se representan con records de Java, sin anotaciones JPA, Lombok ni Spring. La existencia de referencias se verificará en casos de uso y ya está protegida por la base; el modelo por sí solo no consulta repositorios. No se agrega validación de longitud, signo de IDs ni orden de fechas de auditoría no especificada en el contrato.

No incluye snapshots, puertos, casos de uso, endpoints, JWT, frontend, CI o Sonar. La arquitectura hexagonal se completa por incrementos, no solo creando carpetas.
