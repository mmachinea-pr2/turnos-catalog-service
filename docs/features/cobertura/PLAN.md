# Plan de cobertura

1. Agregar JaCoCo 0.8.15 al POM, con prepare-agent y report durante verify.
2. Ejecutar clean verify con Java 25 y Docker para las pruebas MySQL existentes.
3. Verificar reportes HTML/XML, contadores y ausencia de errores.
4. Documentar cómo consultar la medición y cómo activar CI/Sonar.

Rama: `7-config-cobertura-pruebas-jacoco`, desde main. La mejora no depende del PR de modelos de dominio. Al integrar esos modelos y volver a ejecutar verify se medirán también sus pruebas.

Issue #7: Configurar cobertura de pruebas con JaCoCo. Tipo Task; etiqueta enhancement. PR hacia main con revisión docente.

## Referencias técnicas

- [Plugin Maven de JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/maven.html): ejecución del agente e informes.
- [Historial oficial](https://www.jacoco.org/jacoco/trunk/doc/changes.html): versión estable 0.8.15 y soporte de Java 25 desde 0.8.14.

La ampliación configura Backend CI en PR hacia main y push a main, con checkout completo, Java 25, verify y sonar:sonar. El Quality Gate se espera y puede hacer fallar el job. La configuración remota se documenta en [guía de Sonar](../../sonar-ci.md).
