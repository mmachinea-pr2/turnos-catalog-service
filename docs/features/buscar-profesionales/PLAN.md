# Plan de búsqueda por nombre

1. Incorporar name como parámetro opcional del listado actual.
2. Normalizar el texto en la capa de aplicación.
3. Filtrar por coincidencia literal en MySQL conservando categoría, bajas lógicas y orden.
4. Probar el contrato ampliado y conservar las pruebas existentes.
5. Ejecutar clean verify y documentar el uso.

Rama: `23-buscar-profesionales-por-nom`. Issue: [#23](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/23).

Parte de main con la consulta por categoría integrada. El PR se dirigirá a main para revisión docente.
