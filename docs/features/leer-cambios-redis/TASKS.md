# Tareas

- [x] Modelo inmutable y puerto de lectura por versión.
- [x] Adaptador Redis con distinción entre ausente y vacío.
- [x] Validación de versión, JSON e IDs.
- [x] Configuración opcional y errores sin datos sensibles.
- [x] Pruebas y documentación.
- [x] Verificar la suite completa.

Validación del 31/10/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 190 pruebas sin fallos, errores ni omisiones. Redis y MySQL temporales, sin usar el servidor de cátedra.
