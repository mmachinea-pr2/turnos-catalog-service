# Tareas de listado de categorías

- [x] Definir puertos y caso de uso de lectura.
- [x] Implementar filtro y orden en MySQL.
- [x] Agregar controlador y DTO HTTP.
- [x] Documentar contrato y límites.
- [x] Agregar pruebas HTTP de integración.
- [x] Completar clean verify.
- [x] Comprobar el endpoint sobre la copia local.
- [x] Vincular issue #19 y rama `19-listar-categ-habilitadas`.
- [ ] Publicar PR hacia main para revisión docente.

Verificación del 28 de octubre de 2026: clean verify terminó con BUILD SUCCESS; 83 pruebas aprobadas. Una instancia temporal en el puerto local 18081 respondió HTTP 200 con las 8 categorías habilitadas de la copia local y los campos previstos. La instancia se detuvo al finalizar, sin ejecutar sincronización ni modificar datos del catálogo.
