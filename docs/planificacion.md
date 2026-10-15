# Planificación inicial

El objetivo es construir y comprender el sistema completo en etapas verificables. Esta planificación es un borrador propio basado en la consigna; no representa entregas parciales acordadas con el profesor.

## Requisitos que orientan el trabajo

- Aplicación Android con Kotlin Multiplatform y exactamente dos backends Java con Spring Boot.
- Tres repositorios separados, con historial progresivo del desarrollo.
- Datos, permisos de base y migraciones independientes por servicio.
- Búsquedas sobre el catálogo local; reservas consulta la agenda vigente al catálogo.
- Registro e inicio de sesión desde Android, JWT y aislamiento de reservas por usuario.
- Integración con REST, Redis y Kafka de la cátedra.
- Docker Compose para los backends y su infraestructura local; Android fuera de contenedores.
- Pruebas automatizadas de registro, autenticación, sincronización, reservas, autorización e idempotencia.

## Etapas propuestas

| Etapa | Trabajo | Evidencia para avanzar |
| --- | --- | --- |
| 1. Preparación | Revisar herramientas locales, elegir versiones, base de datos y uso de JHipster. Abrir catálogo en IntelliJ. | Decisiones registradas y entorno identificado. |
| 2. Base de catálogo | Crear Spring Boot, configuración externa, base servidor y primera migración. | Arranque reproducible y conexión a base verificada. |
| 3. Usuarios y seguridad | Elegir dónde viven los usuarios e implementar registro, login y validación JWT. | Pruebas de autenticación y rechazo de accesos no autorizados. |
| 4. Catálogo funcional | Implementar snapshot, persistencia, filtros y contrato de consulta. | Búsquedas locales y versión consistente con los datos. |
| 5. Sincronización | Integrar Redis y Kafka, deduplicación y recuperación por snapshot. | Actualización incremental, duplicado y discontinuidad comprobados. |
| 6. Reservas | Crear el segundo backend con su persistencia, contrato JWT, disponibilidad y flujo REST/Kafka. | Reserva confirmada, vencida y cancelada; aislamiento entre usuarios. |
| 7. Android | Crear KMP y completar registro, búsquedas y flujo de reservas. | Casos obligatorios realizables desde la app sin pasos manuales externos. |
| 8. Integración y defensa | Completar recuperación, pruebas, Compose y documentación coherente con el código. | Ejecución reproducible y explicación de las decisiones. |

Las pruebas y la documentación se incorporarán en cada etapa. La integración con Android podrá comenzar antes si los contratos necesarios ya están definidos.

## Acceso a la cátedra

Obtener los datos oficiales de conexión y registrar una única cuenta técnica mediante Postman o una herramienta equivalente. Mantener sus credenciales fuera de Git y de Android. No ejecutar una copia local del servicio central.

## Documentación que se completará

- Diagrama de arquitectura.
- Contratos entre servicios: operaciones, DTO, autenticación y errores.
- Modelo de datos y propiedad de cada servicio.
- Sincronización completa e incremental.
- Estados de reserva, idempotencia y recuperación.
- Seguridad, configuración, ejecución y pruebas.

## Seguimiento

La consigna prevé reuniones quincenales y entrega final en los últimos días de noviembre, con día y horario por confirmar por la cátedra. No se asignan fechas inventadas a estas etapas. Los objetivos parciales se ajustarán con el profesor.

## Próximo paso

La base técnica de catálogo está implementada con Java 25, Spring Boot, Maven, MySQL y Flyway. El primer arranque desde la terminal de IntelliJ fue confirmado por la alumna. Organizar la [primera entrega](primera-entrega.md) mediante milestone, issue y posterior PR antes de continuar con funcionalidades de negocio.
