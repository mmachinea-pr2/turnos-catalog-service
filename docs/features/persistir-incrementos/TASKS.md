# Tareas

- [x] Puerto de persistencia incremental.
- [x] Insertar o actualizar sin eliminar entidades ajenas al lote.
- [x] Exigir versiones consecutivas e ignorar versiones ya aplicadas.
- [x] Guardar datos y estado en una única transacción con bloqueo.
- [x] Probar rollback, bajas lógicas y duplicados concurrentes.
- [x] Documentar límites antes de conectar Redis con la persistencia.
- [x] Verificar la suite completa.

Validación del 31/10/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 231 pruebas sin fallos, errores ni omisiones. Incluye rollback y concurrencia contra MySQL temporal.
