# Tareas de la issue

## Commit 1: recuperación

- [x] Invocar snapshot cuando el coordinador indique que hace falta.
- [x] Mantener los resultados de incremento, reintento y versión adelantada.
- [x] Propagar fallos sin reintentar ni modificar la copia local.
- [x] Registrar el servicio sin ejecutarlo al arrancar.
- [x] Probar recuperación y fallos con MySQL temporal.
- [x] Verificar la suite completa.

Validación del primer incremento, 02/11/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 275 pruebas sin fallos, errores ni omisiones.

## Commit 2: ejecución explícita

- [ ] Agregar una ejecución manual del flujo completo.
- [ ] Informar resultados y errores sin exponer credenciales.
- [ ] Probar el modo de ejecución y documentar sus comandos.
