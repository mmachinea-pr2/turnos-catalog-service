# Tareas

- [x] Modelo y puerto de versiones remotas.
- [x] Adaptador Redis de solo lectura.
- [x] Configuración opcional y ejemplo sin secretos.
- [x] Pruebas de versiones válidas, inválidas, faltantes, ACL y autenticación.
- [x] Verificar la suite completa.

Validación del 31/10/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 141 pruebas, sin fallos ni errores. Redis se probó en un contenedor temporal, no contra el servidor de cátedra.
