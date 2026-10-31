# Plan del estado de sincronización

1. Definir un modelo de consulta que distinga null de versión cero.
2. Agregar puertos, servicio de aplicación y lector JPA sin efectos de escritura.
3. Exponer un DTO HTTP sin información sensible.
4. Probar estado inicial, estado aplicado y errores con MySQL temporal.
5. Ejecutar clean verify y documentar los límites del estado local.

Rama: `29-consultar-estado-de-sincro`. Issue: [#29](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/29).

Incluye como base los commits de búsqueda (#23), horarios (#25) y paginación (#27), todavía pendientes de integrar en main al preparar la rama. Integrar primero esos PR para revisar este incremento por separado. El PR se dirigirá a main para revisión docente.
