# Plan de validación de snapshot

1. Crear CatalogSnapshot en el dominio con versión, fecha y tres listas de modelos existentes.
2. Validar IDs únicos con conjuntos y referencias entre colecciones sin consultas a la base. El recorrido es lineal en la cantidad de registros.
3. Probar casos vacíos, deshabilitados, duplicados, referencias rotas, datos ausentes e inmutabilidad.
4. Ejecutar clean verify para preservar el comportamiento de persistencia y generar cobertura.

Rama: `9-snapshot-de-catalog`, desde main con dominio y cobertura integrados. Issue: [#9](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/9). El PR se dirigirá a main para revisión docente.

El siguiente incremento implementará el reemplazo transaccional de la copia local mediante puertos y adaptadores. Esta entrega no implementa conexiones externas ni frontend.
