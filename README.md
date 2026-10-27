<!-- calidad:inicio -->
![Calidad](https://img.shields.io/badge/Calidad-sin%20c%C3%B3digo-lightgrey) ![Aprobar](https://img.shields.io/badge/Aprobar-no-red) ![Destacar](https://img.shields.io/badge/Destacar-no-lightgrey)

**Calidad (7 dimensiones):** sin código en `main` (no evaluable).
<!-- calidad:fin -->

# Servicio de catálogo y sincronización

Servicio destinado a mantener una copia local de categorías, profesionales y horarios semanales de la cátedra. Las búsquedas y la consulta de agenda se implementarán sobre esa copia.

Proyecto integrador individual de Programación II, 2026. Se utilizan exclusivamente datos ficticios.

## Estado actual

Base técnica implementada: aplicación Spring Boot, conexión MySQL, Flyway, endpoint de salud, modelo persistente del catálogo, modelos de dominio en Java puro y pruebas unitarias y de integración. Se valida la consistencia de un snapshot en memoria. Las búsquedas, la carga y sincronización del catálogo, usuarios y seguridad JWT todavía están pendientes.

## Alcance previsto

- Obtener un snapshot inicial por REST y guardar su versión de forma consistente.
- Actualizar el catálogo mediante notificaciones Kafka y cambios publicados en Redis.
- Recuperar la copia mediante un snapshot cuando falten cambios.
- Buscar por categoría, nombre, estado habilitado y disponibilidad sobre datos locales.
- Informar el estado y los errores de sincronización.
- Proteger las consultas y la comunicación con reservas mediante JWT.

## Tecnologías

Java 25, Spring Boot 4.1.1, Maven Wrapper 3.10.0, MySQL 8.4.8 y Flyway.

Cada backend será propietario de su base lógica o esquema, usuario y migraciones. Docker Compose deberá levantar el servicio y su infraestructura local. No se utilizará una base embebida como persistencia principal.

## Repositorios del sistema

- [Aplicación Android](https://github.com/mmachinea-pr2/turnos-mobile)
- [Catálogo y sincronización](https://github.com/mmachinea-pr2/turnos-catalog-service)
- [Turnos y reservas](https://github.com/mmachinea-pr2/turnos-booking-services)

## Documentación

- [Entorno local comprobado](docs/entorno-local.md)
- [Planificación inicial](docs/planificacion.md)
- [Decisiones y dudas pendientes](docs/decisiones.md)
- [Modelo de datos del catálogo](docs/modelo-datos.md)
- [Etapa de persistencia](docs/persistencia-catalogo.md)
- [Dominio del catálogo](docs/features/dominio-catalogo/SPEC.md)
- [Validación del snapshot](docs/features/validar-snapshot/SPEC.md)

## Fuentes de requisitos

- `PROJECT_STATEMENT-v1.md`: alcance y requisitos de la cátedra.
- `INTEGRATION_REFERENCE-v2.md`: contratos externos REST, Redis y Kafka.

## Ejecutar desde IntelliJ

1. Abrir esta carpeta con **File → Open** e importar el proyecto Maven (`pom.xml`).
2. En **File → Project Structure → Project SDK**, seleccionar Java 25. En esta computadora está instalado en `~/.sdkman/candidates/java/25.0.4.1+1-tem`. Usar también ese JDK en Maven (importación y ejecución).
3. Si `.env` no existe, copiar `.env.example` a `.env` y completar `DB_PASSWORD` y `MYSQL_ROOT_PASSWORD` con claves locales diferentes. En la computadora actual ya se creó el archivo local.
4. En la terminal del proyecto, ejecutar `docker compose up -d --wait mysql`.
5. Abrir `src/main/java/ar/edu/programacion2/catalog/CatalogApplication.java` y crear una configuración de ejecución para esa clase.
6. En **Edit Configurations → Environment variables**, cargar `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_HOST`, `DB_PORT` y `APP_PORT` desde el `.env` local. No pasar `MYSQL_ROOT_PASSWORD` a Java. Spring Boot no lee `.env` automáticamente.
7. Ejecutar la clase y abrir `http://localhost:8080/actuator/health`. Se espera `{"status":"UP"}`.

El puerto MySQL predeterminado es 3308; el HTTP es 8080. Ambos se publican únicamente en la computadora local. El health comprueba disponibilidad técnica y conexión a la base; todavía no hay sincronización con la cátedra.

## Ejecutar desde terminal

Con SDKMAN instalado, dentro de este repositorio:

```bash
sdk env
set -a
. ./.env
set +a
unset MYSQL_ROOT_PASSWORD
docker compose up -d --wait mysql
./mvnw spring-boot:run
```

Java 25 se selecciona para la terminal actual; no hace falta cambiar el Java predeterminado de otros proyectos. Maven se descarga mediante el wrapper.

Para ejecutar también el backend en un contenedor:

```bash
docker compose up --build -d
```

Usar una sola modalidad a la vez para evitar que dos procesos ocupen el mismo puerto HTTP. `docker compose down` detiene los contenedores y conserva el volumen de datos. Eliminar el volumen borra la base; cambiar claves en `.env` no cambia las cuentas de una base ya inicializada.

## Pruebas

```bash
sdk env
./mvnw -B -ntp clean verify
```

Docker debe estar funcionando. Las pruebas crean un MySQL temporal independiente, sin usar `.env` ni la base de desarrollo. Comprueban aplicación de una migración, escritura y lectura de datos y respuesta HTTP de salud sin detalles internos. Los informes quedan en `target/surefire-reports`.

La migración de producción `V1__create_catalog.sql` crea categorías, profesionales, horarios y estado de sincronización. Reemplaza la antigua tabla exclusiva de pruebas `setup_probe`. Flyway administra el esquema y Hibernate lo valida (`ddl-auto=validate`). Las pruebas verifican también referencias, restricciones de horarios y rollback conjunto de datos y versión. Una vez aplicada una migración de producción, los cambios futuros requieren una nueva versión; no se modifica la anterior.

## Cobertura de pruebas

`./mvnw -B -ntp clean verify` ejecuta las pruebas y genera la medición con JaCoCo 0.8.15:

- `target/site/jacoco/index.html`: abrir en un navegador para consultar clases y líneas ejecutadas.
- `target/site/jacoco/jacoco.xml`: datos para herramientas de análisis como Sonar.

Docker debe estar funcionando para las pruebas de integración. Los reportes se generan localmente y no se suben al repositorio. No se definieron exclusiones propias ni un mínimo obligatorio de cobertura. El workflow de GitHub Actions genera este XML antes de enviarlo a Sonar. La activación requiere configurar la cuenta según la [guía de CI y Sonar](docs/sonar-ci.md).

Ver [especificación de cobertura](docs/features/cobertura/SPEC.md).

## Configuración local

`.env` contiene claves de desarrollo y está excluido de Git y de la imagen Docker. `.env.example` documenta los campos sin secretos. No hay credenciales reales de cátedra ni integración externa en esta etapa.

## Referencia técnica

La base se generó mediante [Spring Initializr](https://start.spring.io/).
