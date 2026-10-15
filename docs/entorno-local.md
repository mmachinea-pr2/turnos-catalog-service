# Entorno local de desarrollo

Comprobación realizada el 15 de octubre de 2026. Este inventario describe la computadora de desarrollo; no fija todavía todas las versiones del proyecto.

| Herramienta | Resultado |
| --- | --- |
| IntelliJ IDEA | 2026.2.3, instalado mediante JetBrains Toolbox. |
| Java activo en terminal | Amazon Corretto 21.0.2; compilador javac 21.0.2 disponible. |
| Otro JDK 21 disponible | OpenJDK 21.0.4 en `/usr/lib/jvm/java-21-openjdk-amd64`. |
| Docker | Cliente y servidor 29.8.0; servidor accesible con el usuario actual. |
| Docker Compose | v2.29.7. |
| Maven y Gradle | No encontrados en PATH. El proyecto podrá incluir un wrapper para ejecutar su herramienta de construcción. |

## Java e IntelliJ

El Java activo de la terminal se encuentra en `~/.sdkman/candidates/java/current`. Para el catálogo, seleccionar Java 25 como Project SDK y como JDK de Maven; Java 21 era la versión activa durante el inventario inicial. La versión configurada dentro del IDE aún no se ha comprobado.

## Docker

El usuario pertenece al grupo `docker` y se verificó la respuesta del servidor. No fue necesario cambiar permisos del sistema ni iniciar contenedores.

## Preparación del catálogo

Se instaló Temurin Java 25 mediante SDKMAN en `~/.sdkman/candidates/java/25.0.4.1+1-tem`. El archivo `.sdkmanrc` permite seleccionarlo con `sdk env`. La aplicación utiliza Maven Wrapper 3.10.0, Spring Boot 4.1.1 y MySQL 8.4.8.

## Próximo paso

Importar el proyecto Maven en IntelliJ, seleccionar Java 25 y cargar las variables de desarrollo según el README. La selección del SDK dentro del IDE debe comprobarse al abrir el proyecto.

## Validación del primer arranque

- `./mvnw -B -ntp verify`: BUILD SUCCESS; dos pruebas ejecutadas, sin fallos ni errores.
- `docker compose up --build -d`: backend construido y ejecutado junto con MySQL.
- `GET http://127.0.0.1:8080/actuator/health`: HTTP 200 y `{"status":"UP"}`.
- MySQL figura healthy y publica su puerto únicamente en 127.0.0.1:3308.
- Se comprobó que la migración exclusiva de pruebas no forma parte del JAR.
- `.env` y `target/` están excluidos de Git.

Los contenedores de desarrollo quedaron activos. Antes de ejecutar Java desde IntelliJ, detener únicamente el backend con `docker compose stop catalog`; la base MySQL puede continuar activa. No se realizaron commits, push ni PR.
