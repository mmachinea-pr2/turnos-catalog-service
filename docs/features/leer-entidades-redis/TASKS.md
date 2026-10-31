# Tareas

- [x] Modelo inmutable y puerto de lectura de entidades.
- [x] Lectura selectiva de los tres Hashes.
- [x] Validación de JSON, IDs, tipos, fechas y horarios.
- [x] Conservar bajas lógicas y rechazar lotes incompletos.
- [x] Pruebas y documentación de límites de consistencia.
- [x] Verificar la suite completa.

Validación del 31/10/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 221 pruebas sin fallos, errores ni omisiones. MySQL y Redis temporales, sin consultar el servidor docente.
