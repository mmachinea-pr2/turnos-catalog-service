# Tareas

- [x] Conectar lectores y puerto de persistencia.
- [x] Validar versión e IDs del lote recibido.
- [x] Releer metadatos antes de guardar y descartar lotes si cambian.
- [x] Informar necesidad de snapshot o reintento sin ejecutarlos aún.
- [x] Probar errores y flujo con MySQL temporal.
- [x] Documentar límites de consistencia remota.
- [x] Verificar la suite completa.

Validación del 01/11/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 259 pruebas sin fallos, errores ni omisiones. Incluye coordinación con persistencia real en MySQL temporal y lectores externos simulados.
