# Plan de lectura de entidades Redis

1. Definir resultado inmutable y puerto de lectura de entidades.
2. Leer únicamente los IDs solicitados con el cliente Redis existente.
3. Convertir los JSON a modelos de dominio y validar los campos.
4. Rechazar lotes incompletos sin exponer datos remotos en errores.
5. Probar con Redis temporal y documentar límites de consistencia.

Rama: `37-leer-entidades-modificadas-redis`. Issue: [#37](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/37). Base: `92ed8ce`, con los PR #34 y #36 integrados en main. Milestone: Persistencia y sincronización del catálogo. El PR irá a main para revisión docente.
