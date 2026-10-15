# Organización de la primera entrega de catálogo

Propuesta local para registrar en GitHub. El trabajo técnico ya se realizó y verificó localmente; la issue documentará ese avance para su revisión. No se crearon milestones, issues, commits ni PR remotos.

## Milestone

Título: **M1 — Base técnica del servicio de catálogo**

Descripción para copiar:

> Preparar un servicio de catálogo ejecutable con Java 25 y Spring Boot, conectado a una base MySQL propia. Incorporar Maven Wrapper, Flyway, Docker Compose, configuración externa, pruebas de integración y documentación de ejecución. Esta etapa establece la infraestructura inicial; las entidades de negocio, búsquedas, sincronización y seguridad JWT se implementarán en etapas posteriores.

Fecha: dejar sin vencimiento hasta acordarla con el profesor.

## Issue

Título: **Preparar la base técnica del servicio de catálogo**

Milestone: **M1 — Base técnica del servicio de catálogo**.

Etiqueta sugerida: `enhancement`, si está disponible. Asignación: la alumna.

Cuerpo para copiar:

```markdown
## Objetivo

Disponer de una base ejecutable y reproducible del servicio de catálogo para implementar luego las funcionalidades del sistema de turnos.

## Alcance

- Java 25 y Spring Boot 4.1.1, con Maven Wrapper 3.10.0.
- MySQL 8.4.8 con usuario de aplicación y volumen propios.
- Flyway para migraciones y Hibernate en modo validate.
- Docker Compose para la base y el backend.
- Variables de entorno y archivo de ejemplo sin secretos.
- Endpoint de salud y pruebas con MySQL temporal mediante Testcontainers.
- README y documentación inicial de planificación y decisiones.

## Criterios de aceptación

- [x] El proyecto compila con Java 25 mediante Maven Wrapper.
- [x] Docker Compose permite levantar MySQL y el backend.
- [x] El servicio puede ejecutarse desde la terminal de IntelliJ.
- [x] GET /actuator/health responde HTTP 200 con {"status":"UP"}.
- [x] Las pruebas verifican migración Flyway, escritura y lectura en MySQL y respuesta de salud.
- [x] Las pruebas usan una base temporal independiente de la base de desarrollo.
- [x] .env y archivos de compilación están excluidos de Git.
- [x] El README explica configuración, ejecución y pruebas.
- [ ] Los cambios están publicados en un PR hacia la rama indicada por el profesor.
- [ ] El profesor revisó y aceptó la entrega según el flujo acordado.

## Evidencia local

- ./mvnw -B -ntp verify: BUILD SUCCESS; 2 pruebas sin fallos ni errores.
- docker compose up --build -d: arranque del backend y MySQL verificado.
- La alumna confirmó {"status":"UP"} después de ejecutar desde la terminal de IntelliJ.
- La migración setup_probe es exclusiva de pruebas y no se incluye en el JAR.

## Pendiente para otras etapas

Entidades y tablas de negocio, búsquedas, sincronización REST/Redis/Kafka, autenticación y autorización JWT. Esta issue no representa un catálogo funcional completo.

## Estado al registrar la issue

Implementación y validación locales realizadas antes de crear la issue. Pendiente de publicación y revisión docente.
```

## Publicación y revisión

1. Crear el milestone en el repositorio de catálogo.
2. Crear la issue y asociarla al milestone; anotar su número real.
3. Usar una rama de trabajo por issue. El milestone agrupa issues y no necesita una rama propia. Todos los PR de entrega apuntan a `dev`, donde revisa el profesor.
4. Revisar el diff y los archivos a incluir. El archivo .env no se publica.
5. Crear la rama de trabajo desde `dev` una vez verificado su estado. Para la issue #1 se creó `1-preparacion`. Realizar el commit y hacer push cuando la alumna lo indique.
6. Abrir el PR desde la rama de trabajo hacia `dev` y enlazar la issue mediante `Refs #1`. El profesor acepta o rechaza el PR. No dirigir entregas a `main` ni integrarlas allí antes de la revisión docente.
7. Mantener la issue abierta hasta cumplir la revisión acordada; cerrar issue y milestone cuando corresponda.

La alumna creó en GitHub las ramas `dev` y `1-preparacion`. Se ejecutó `git fetch origin` y se cambió el catálogo local a `1-preparacion`, vinculada a `origin/1-preparacion`, conservando los cambios sin commit. El destino del futuro PR es `dev`. No se hizo push del código. La integración posterior de `dev` a `main` queda fuera de esta entrega y requiere acordar su procedimiento.

## Flujo de ramas

`dev` → rama de la issue → PR hacia `dev` → revisión del profesor.

El milestone agrupa entregas relacionadas. Cada issue tiene su rama y su PR; no hace falta duplicar ramas para el milestone.

## Borrador de PR para después

Título: **Preparar la base técnica del servicio de catálogo**

Descripción:

> El repositorio contaba solo con documentación inicial. Se incorpora una aplicación Spring Boot con Java 25, Maven Wrapper, MySQL, Flyway y Docker Compose, junto con configuración externa e instrucciones de ejecución.
>
> Validación: Maven verify finaliza correctamente con dos pruebas de integración contra MySQL temporal. Se comprobó el arranque con Compose y desde la terminal de IntelliJ, y la respuesta HTTP 200 del endpoint de salud. Las funcionalidades de negocio y JWT quedan pendientes.
>
> Refs #1

Actualizar las evidencias si cambia la implementación antes del PR.
