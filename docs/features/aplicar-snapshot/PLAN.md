# Plan de persistencia del snapshot

1. Definir el puerto de salida CatalogSnapshotStore.
2. Implementar reemplazo y comparación de versión bajo bloqueo y transacción.
3. Probar carga inicial, reemplazo, repetición, versión anterior, catálogo vacío y rollback real con MySQL temporal.
4. Ejecutar clean verify y revisar el informe de cobertura.

Rama: `11-persistir-el-snapshot-de-catalog`. Issue: [#11](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/11).

Depende del commit de validación del snapshot del issue #9, incorporado a esta rama. Para revisar un PR independiente hacia main, primero debe integrarse esa dependencia; hasta entonces la comparación también incluye los cambios del #9.
