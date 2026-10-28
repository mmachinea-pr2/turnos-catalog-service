# Plan de sincronización por snapshot

1. Definir puerto de entrada y servicio de aplicación independiente de Spring.
2. Conectar los puertos de consulta y persistencia mediante configuración opcional.
3. Probar el recorrido completo y los fallos sin tocar la base de desarrollo.
4. Ejecutar clean verify y documentar el alcance.

Rama: `15-integrar-consulta-y-persistencia-de-snapshot`. Issue: [#15](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/15).

Depende del cliente REST del issue #13, cuyo commit está incorporado en esta rama. Integrar primero ese PR para revisar este avance por separado hacia main.
