# Plan del cliente REST

1. Definir el puerto CatalogSnapshotSource.
2. Mapear DTO externos a modelos validados, distinguiendo ausencia de valores por defecto.
3. Configurar autenticación, tiempos máximos y activación opcional.
4. Probar respuestas y errores con un servidor simulado, sin acceso a la cátedra.
5. Ejecutar clean verify.

Rama: `13-obtener-el-snpshot-de-catalog-rest`. Issue: [#13](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/13).

La rama parte de main con los avances de validación y persistencia del snapshot ya integrados. El PR se dirigirá a main para revisión docente.
