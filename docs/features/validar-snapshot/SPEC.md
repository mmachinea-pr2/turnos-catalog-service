# Validar un snapshot completo de catálogo

## Objetivo

Representar un catálogo completo consistente antes de que un futuro caso de uso intente reemplazar la copia local. El modelo se basa en los campos de la sección de snapshot de INTEGRATION_REFERENCE-v2.md.

## Reglas

- Versión no negativa y fecha de generación obligatoria. Cero es una versión válida.
- Las tres colecciones son obligatorias, admiten estar vacías y no admiten elementos nulos.
- No se repiten IDs dentro de una colección; el mismo número puede existir en tipos diferentes.
- Cada profesional referencia una categoría presente en el snapshot.
- Cada horario referencia un profesional presente en el snapshot.
- Se conservan registros deshabilitados; también deben tener referencias válidas.
- Se copian las listas a colecciones inmutables para que los datos no cambien después de validarse.

## Límites

Este modelo interno de dominio no es un DTO HTTP ni carga datos desde REST. No modifica MySQL ni la versión persistida. Las reglas individuales de horario ya las aplica WeeklySchedule. La comparación con la versión local, las transacciones y la recuperación de errores pertenecen al siguiente caso de uso.

La versión es un long interno. El futuro DTO de entrada debe distinguir una versión ausente de cero antes de construir este modelo. No se interpreta una lista ausente como una lista vacía.
