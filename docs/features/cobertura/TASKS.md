# Tareas de cobertura

- [x] Configurar agente e informes JaCoCo en Maven.
- [x] Documentar ejecución y alcance.
- [x] Verificar pruebas e informes generados.
- [x] Vincular issue #7 y rama `7-config-cobertura-pruebas-jacoco`.
- [ ] Publicar mediante PR hacia main para revisión docente.

Validación del 23 de octubre de 2026: `clean verify` finalizó con BUILD SUCCESS y seis pruebas aprobadas. Se verificó la generación de HTML y XML. Medición de la base de main usada en esta rama: LINE: 52/67 (77.6%); BRANCH: 8/8 (100.0%). Estos valores no incluyen los modelos de dominio aún pendientes de integrar y deben recalcularse al cambiar el código.

## Integración con CI y Sonar

- [x] Preparar workflow y scanner Maven con importación del XML.
- [x] Documentar configuración del token, organización y método de análisis.
- [ ] Configurar los valores en GitHub y desactivar Automatic Analysis en Sonar.
- [ ] Comprobar la primera ejecución remota y la importación de cobertura.

Validación local de la ampliación CI: YAML parseado correctamente y clean verify con seis pruebas aprobadas e informe XML generado. La ejecución de Actions y la importación real en Sonar siguen pendientes de publicación y configuración del secreto.
