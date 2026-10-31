# Tareas del estado de sincronización

- [x] Definir modelo, puertos y caso de uso de consulta.
- [x] Implementar lectura JPA del estado local.
- [x] Exponer DTO HTTP con initialized, appliedVersion y appliedAt.
- [x] Agregar pruebas de integración y documentación.
- [x] Completar clean verify.
- [x] Vincular issue #29 y rama `29-consultar-estado-de-sincro`.
- [ ] Publicar PR para revisión docente.

Verificación local del 30 de octubre de 2026: clean verify terminó con BUILD SUCCESS; 119 pruebas aprobadas, incluidas 4 nuevas. Se comprobó el contrato HTTP contra MySQL temporal sin consultar la cátedra ni modificar la copia de desarrollo.
