# Plan de consulta de horarios

1. Definir puertos y caso de uso para la agenda semanal.
2. Filtrar horarios y entidades relacionadas por estado habilitado en MySQL.
3. Ordenar días y horas en la aplicación y exponer DTO propio por HTTP.
4. Probar contrato, filtros, resultados vacíos e IDs inválidos.
5. Ejecutar clean verify y documentar las diferencias con disponibilidad.

Rama: `25-consultar-horarios-semanales-por-profesional`. Issue: [#25](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/25).

Incluye como base el commit de búsqueda por nombre del issue #23, todavía pendiente de integrar en main al preparar la rama. Integrar primero ese PR para revisar este incremento por separado. El PR se dirigirá a main para revisión docente.
