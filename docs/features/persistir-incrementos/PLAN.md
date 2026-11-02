# Plan de persistencia incremental

1. Definir un puerto de guardado de lotes ya leídos.
2. Reutilizar el bloqueo de estado para serializar snapshots e incrementos.
3. Validar orden de versiones e insertar o actualizar sin borrar otras filas.
4. Confirmar datos, versión e instante en una sola transacción.
5. Probar rollback y concurrencia con MySQL temporal.

Rama: `39-persistir-incrementos-del-catalogo`. Issue: [#39](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/39). Base: `c10d73f`, lectura de entidades (#37). Milestone: Persistencia y sincronización del catálogo. El PR irá a main después de integrar los avances anteriores.
