# Plan de coordinación incremental

1. Definir resultados explícitos para aplicación, snapshot y reintento.
2. Coordinar lectores sin abrir transacción durante accesos externos.
3. Validar correspondencia del lote y releer metadatos antes de guardar.
4. Solicitar snapshot cuando no se pueda aplicar una única versión vigente.
5. Probar errores, cambios concurrentes y persistencia real.

Rama: `41-coordinar-lect-y-persistencia-de-un-incremento`. Issue: [#41](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/41). Base: `46724b2`, con los PR #38 y #40 integrados en main. Milestone: Persistencia y sincronización del catálogo. El PR irá a main para revisión docente.
