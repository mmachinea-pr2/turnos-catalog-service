# Plan de paginación

1. Definir parámetros y resultado de aplicación independientes de Spring Data.
2. Limitar la consulta en MySQL manteniendo los filtros existentes.
3. Exponer page, size y X-Total-Count sin cambiar el DTO del array.
4. Probar varias páginas, totales y entradas inválidas.
5. Ejecutar clean verify y documentar el cambio de tamaño predeterminado.

Rama: `27-paginar-consulta-de-porfecionales`. Issue: [#27](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/27).

Incluye como base los commits de búsqueda por nombre (#23) y horarios (#25), todavía pendientes de integrar en main al preparar la rama. Integrar primero esos PR para revisar la paginación por separado. El PR se dirigirá a main para revisión docente.
