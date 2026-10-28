# Tareas de sincronización por snapshot

- [x] Definir puerto de entrada y servicio de aplicación.
- [x] Conectar consulta y persistencia mediante configuración Spring opcional.
- [x] Probar el flujo completo, errores y arranque sin efectos.
- [x] Completar clean verify.
- [x] Vincular issue #15 y rama `15-integrar-consulta-y-persistencia-de-snapshot`.
- [ ] Publicar PR hacia main para revisión docente.

Validación local del 27 de octubre de 2026: clean verify terminó con BUILD SUCCESS; 73 pruebas aprobadas, 6 nuevas. El flujo REST → caso de uso → MySQL se probó con respuestas simuladas y una base temporal. No se ejecutó contra el servidor de la cátedra ni contra la base de desarrollo.
