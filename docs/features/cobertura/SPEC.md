# Cobertura de pruebas del catálogo

## Objetivo

Medir las líneas y ramas ejecutadas por las pruebas del backend para detectar partes sin verificar y generar el XML que podrá consumir Sonar en una etapa posterior.

## Criterios de aceptación

- Maven verify ejecuta las pruebas con el agente de JaCoCo.
- Se generan HTML legible y XML en target/site/jacoco.
- Los resultados reflejan las clases de producción sin exclusiones propias para aumentar artificialmente el porcentaje.
- Un fallo de las pruebas sigue haciendo fallar el build.
- No se versionan reportes generados ni se necesitan secretos de Sonar para medir localmente.

## Límites

Se amplía la etapa con un workflow de GitHub Actions que ejecuta verify y publica el informe mediante SonarScanner Maven. La activación remota depende de configurar el token, la clave de organización y desactivar Automatic Analysis. No fija un porcentaje mínimo no acordado. Un porcentaje alto no demuestra cumplimiento funcional ni ausencia de errores.
