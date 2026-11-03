# Tareas de la issue

## Commit 1: contrato y procesamiento

- [x] Validar CatalogUpdated y su message key.
- [x] Registrar eventId en MySQL con una nueva migración.
- [x] Sincronizar antes de registrar y comprobar la versión local.
- [x] Manejar duplicados, eventos atrasados, datos todavía no publicados y fallos.
- [x] Probar concurrencia y persistencia real.
- [x] Verificar la suite completa.

Validación del primer incremento, 02/11/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 339 pruebas sin fallos, errores ni omisiones. Incluye migración V2, concurrencia e idempotencia con MySQL temporal. Todavía no se prueba un consumidor conectado a Kafka.

## Commit 2: consumidor

- [x] Configurar Kafka con topic y grupo propios mediante variables externas.
- [x] Conectar el listener y controlar confirmaciones y errores.
- [x] Probar consumo con Kafka local y documentar la ejecución.

Validación del segundo incremento, 02/11/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 357 pruebas sin fallos, errores ni omisiones. Kafka local en Testcontainers verifica confirmaciones, detención ante fallos, reentrega al reiniciar y duplicados con un procesador controlado. La persistencia e idempotencia en MySQL se verifican por separado. `docker compose config --quiet` también pasó. Queda pendiente comprobar la conexión y recepción de eventos del servidor de cátedra.
