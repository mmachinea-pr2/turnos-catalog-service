# Configurar CI y Sonar del catálogo

El workflow Backend CI ejecuta Maven con Java 25 y genera cobertura JaCoCo en cada PR hacia main y cada push a main. Testcontainers usa Docker del runner y una base MySQL temporal; no requiere .env ni credenciales de cátedra.

## Configuración en las cuentas

1. La clave de organización `mmachinea-pr2` fue verificada en la API pública de Sonar y está configurada en el POM.
2. Crear un token de análisis desde My Account → Security. Si ya hay un token válido para este proyecto, puede reutilizarse. Guardarlo directamente en GitHub, no en archivos ni mensajes.
3. En el repositorio GitHub: Settings → Secrets and variables → Actions → Secrets → New repository secret. Nombre SONAR_TOKEN; valor: el token.
4. Cuando el workflow esté listo para publicarse, desactivar Automatic Analysis en Sonar → Administration → Analysis Method y seleccionar GitHub Actions. No ejecutar ambos métodos sobre el mismo proyecto.
5. Publicar el commit del workflow en la rama de trabajo. En el PR, consultar Actions y el check Pruebas y Sonar.

El project key configurado es mmachinea-pr2_turnos-catalog-service, tomado del análisis existente. Los secretos solo se entregan al paso de análisis. En PR de forks se ejecutan las pruebas pero no Sonar, porque GitHub no entrega esos secretos.

## Resultado esperado

El build debe pasar, existir target/site/jacoco/jacoco.xml y finalizar el scanner con la importación de cobertura. El workflow espera el Quality Gate: un rechazo o timeout lo hace fallar. No modifica reglas de protección ni acepta o fusiona PR.

La cobertura global y la de código nuevo son métricas distintas. Un PR de configuración puede no agregar líneas Java medibles; no se exige que ese indicador coincida con la cobertura global del proyecto.

## Diagnóstico

- Falta SONAR_TOKEN: revisar el secreto del repositorio.
- Conflicto de análisis automático: desactivar Automatic Analysis.
- Error de autorización: revisar permisos del token y correspondencia de proyecto/organización.
- Cobertura ausente: comprobar el paso de JaCoCo y buscar en el log del scanner JaCoCo XML Report Importer.
- Quality Gate rojo: revisar los hallazgos y métricas en Sonar; el token puede funcionar aunque la calidad no alcance el umbral.

## Alcance de la validación

Las pruebas y generación del XML se verifican localmente. La publicación real, los permisos del token y el resultado remoto solo pueden comprobarse después de configurar los valores y ejecutar GitHub Actions. No se envían análisis desde la computadora durante esta preparación.

Fuentes: [GitHub Actions con Sonar](https://docs.sonarsource.com/sonarqube-cloud/analyzing-source-code/ci-based-analysis/github-actions-for-sonarcloud), [SonarScanner Maven](https://docs.sonarsource.com/sonarqube-cloud/analyzing-source-code/scanners/sonarscanner-for-maven).
