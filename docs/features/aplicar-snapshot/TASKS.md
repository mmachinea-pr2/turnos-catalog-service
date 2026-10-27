# Tareas de persistencia del snapshot

- [x] Definir puerto de salida sin dependencias de infraestructura.
- [x] Implementar reemplazo transaccional con control de versión.
- [x] Agregar pruebas de integración contra MySQL.
- [x] Completar clean verify y revisar cobertura.
- [x] Vincular issue #11 y rama `11-persistir-el-snapshot-de-catalog`.
- [ ] Publicar PR hacia main para revisión docente.

Validación local del 26 de octubre de 2026: `clean verify` finalizó con BUILD SUCCESS. Se aprobaron 34 pruebas, incluidas 6 nuevas de persistencia del snapshot. Se generaron los informes HTML y XML de JaCoCo. Las pruebas utilizaron bases temporales MySQL; no se modificó la base de desarrollo.
