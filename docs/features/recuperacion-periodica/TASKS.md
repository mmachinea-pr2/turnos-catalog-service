# Tareas

- [x] Reutilizar la sincronización mediante una comprobación por invocación.
- [x] Evitar invocaciones superpuestas sobre la misma instancia.
- [x] Permitir otra comprobación tras fallos sin filtrar información sensible.
- [x] Agregar pruebas de resultados, errores y concurrencia.
- [x] Registrar el temporizador opcional y configurar activación e intervalo.
- [x] Documentar variables y verificar configuración y cierre.

Validación del segundo incremento: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 313 pruebas sin fallos, errores ni omisiones en esta rama. Incluye 24 pruebas de la tarea y su configuración. `docker compose config --quiet` también pasó. No incluye todavía el PR de Kafka pendiente de integración.
