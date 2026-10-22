# Tareas de persistencia del catálogo

## Diseño preparado para el primer commit

- [x] Definir alcance y criterios de aceptación en SPEC.
- [x] Documentar tablas, relaciones y tipos en el modelo de datos.
- [x] Definir estado inicial de sincronización y reglas transaccionales.
- [x] Describir secuencia de implementación y verificación en PLAN.

## Implementación de persistencia

- [x] Revisar e incorporar la migración de producción.
- [x] Revisar e incorporar entidades y repositorios JPA.
- [x] Incorporar pruebas y retirar la migración exclusiva de prueba anterior.
- [x] Actualizar README y decisiones según la implementación incorporada.
- [x] Registrar el resultado de clean verify para la versión que se publicará.

La implementación corresponde a la issue #3. La publicación y la revisión docente permanecen pendientes; no se cierra la issue con este commit.

Verificación previa al commit: `./mvnw -B -ntp clean verify` completado el 22 de octubre de 2026. Seis pruebas aprobadas, sin errores ni omisiones, con MySQL temporal.
