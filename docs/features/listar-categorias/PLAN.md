# Plan del listado de categorías

1. Definir puertos de entrada y lectura, con dominio independiente de Spring.
2. Consultar categorías habilitadas y ordenadas en el adaptador JPA.
3. Exponer un DTO propio en GET /api/catalog/categories.
4. Probar el recorrido HTTP → aplicación → MySQL y ejecutar clean verify.
5. Comprobar el listado sobre la copia local cargada.

Rama: `19-listar-categ-habilitadas`. Issue: [#19](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/19).

Incluye como base los cambios de carga manual del issue #17, todavía pendientes de integrar en main al preparar la rama. Integrar primero ese PR para revisar el listado de categorías por separado. El PR se dirigirá a main para revisión docente.
