# Tareas de la issue

## Commit 1: contrato y procesamiento

- [x] Validar CatalogUpdated y su message key.
- [x] Registrar eventId en MySQL con una nueva migración.
- [x] Sincronizar antes de registrar y comprobar la versión local.
- [x] Manejar duplicados, eventos atrasados, datos todavía no publicados y fallos.
- [x] Probar concurrencia y persistencia real.
- [x] Verificar la suite completa.

Validación del primer incremento, 02/11/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 339 pruebas sin fallos, errores ni omisiones. Incluye migración V2, concurrencia e idempotencia con MySQL temporal. Todavía no se prueba un consumidor conectado a Kafka.

## Siguiente commit: consumidor

- [ ] Configurar Kafka con topic y grupo propios mediante variables externas.
- [ ] Conectar el listener y controlar confirmaciones y errores.
- [ ] Probar consumo con Kafka local y documentar la ejecución.
