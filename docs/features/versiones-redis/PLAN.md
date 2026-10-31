# Plan de lectura de versiones Redis

1. Definir resultado y puerto de lectura sin dependencias de infraestructura.
2. Agregar Lettuce y configuración opcional con credenciales externas y tiempos límite.
3. Leer las dos versiones juntas y rechazar metadatos incompletos o inconsistentes.
4. Probar con un Redis temporal y un usuario sin permisos de escritura.
5. Ejecutar la suite completa y documentar el alcance.

Rama: `31-consultar-versiones-de-catalogo-redis`. Issue: [#31](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/31). El PR se dirigirá a main para revisión docente.

Base: `9393a20`, con el PR #30 y los incrementos anteriores integrados en main.
