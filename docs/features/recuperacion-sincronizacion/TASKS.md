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

- [x] Agregar una ejecución manual del flujo completo.
- [x] Informar resultados y errores sin exponer credenciales.
- [x] Probar el modo de ejecución y documentar sus comandos.
- [x] Verificar la suite completa del segundo incremento.

Validación del segundo incremento, 02/11/2026: `./mvnw -B -ntp clean verify`, BUILD SUCCESS; 289 pruebas sin fallos, errores ni omisiones. Incluye el comando real con Redis y MySQL temporales y un servidor HTTP simulado.
