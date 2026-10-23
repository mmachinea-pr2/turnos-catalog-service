# Decisiones y dudas pendientes

Este registro distingue requisitos de la cátedra, organización inicial y elecciones técnicas todavía abiertas. Se actualizará con los motivos y consecuencias de cada decisión propia.

## Requisitos confirmados por los documentos

| Tema | Requisito |
| --- | --- |
| Componentes | Android KMP y exactamente dos backends Java con Spring Boot. |
| Persistencia | Gestor servidor, datos separados y migraciones propias. H2 y SQLite no sirven como persistencia principal. |
| Comunicación | Contratos explícitos entre servicios protegidos con JWT. Sin acceso cruzado a tablas o repositorios. |
| Catálogo | Una única réplica local vigente, propiedad del servicio de catálogo. |
| Identidades | Cuenta técnica de cátedra distinta de los usuarios finales. |
| Seguridad | Contraseñas con hashing adecuado, validación JWT y reservas aisladas por usuario. |
| Integración | REST, Redis y Kafka según el anexo técnico. |
| JHipster | Opcional; el usuario final debe conservar la compatibilidad requerida por la consigna. |

## Organización inicial

Los tres repositorios fueron creados por la alumna. La planificación general y este registro se ubican inicialmente en catálogo; los README de los otros repositorios los referencian. Esta ubicación puede revisarse sin cambiar los límites entre los servicios.

## Flujo de entrega acordado

El trabajo actual se limita al catálogo. El flujo actualizado es rama de trabajo → PR hacia `main` → revisión y aceptación del profesor. No se hace push directo a `main`. Cada issue puede tener su rama; el milestone agrupa issues. Los commits y la publicación quedan a cargo de la alumna salvo indicación expresa.

## Decisiones tomadas para el primer arranque

- Spring Boot directo, sin JHipster, para configurar explícitamente las dependencias y los componentes necesarios del servicio.
- Java 25 y Spring Boot 4.1.1; Maven Wrapper 3.10.0 para compilar sin instalación global de Maven.
- MySQL 8.4.8 en un contenedor y volumen propios de catálogo. Usuario de aplicación distinto de root.
- Flyway como responsable de migraciones y Hibernate en modo de validación.
- Configuración mediante variables de entorno y `.env` local excluido de Git.
- Testcontainers para verificar la base técnica con MySQL real y aislado.
- Puerto local MySQL 3308 para reducir conflictos con otras instalaciones; configurable.

Estas elecciones no resuelven todavía el diseño de las entidades, la seguridad o la sincronización.

## Decisiones técnicas abiertas

| Tema | Qué falta decidir |
| --- | --- |
| Versiones mobile | Kotlin y herramientas Android compatibles con el entorno disponible. |
| Arquitectura interna | Organización de paquetes y responsabilidades. |
| Usuarios | Cuál de los dos servicios almacenará usuarios y emitirá sus JWT, sin agregar un tercer backend. |
| JWT entre servicios | Propagar el token del usuario o utilizar un token técnico; definir validación y autorización. |
| Reservas | Estados, transiciones, persistencia del progreso y tratamiento de eventos tardíos. |
| Idempotencia | Cómo detectar duplicados y evitar repetir efectos. |
| Publicación Kafka | Cómo recuperar envíos interrumpidos y mantener consistencia con la base. |
| Actualización de Android | Cómo conocer cambios de estado de una reserva. |
| Recuperación | Timeouts, reintentos limitados y reconciliación tras respuestas perdidas. |

## Dudas para aclarar con la cátedra

- Significado exacto del filtro de disponibilidad en las búsquedas sobre datos locales.
- Validaciones detalladas para el registro de usuarios finales compatibles con JHipster; no confundirlas con las de la cuenta técnica.
- Zona horaria de los horarios de atención.
- Datos oficiales de acceso al entorno y objetivos del primer seguimiento.

## Criterio para incorporar referencias

La consigna define el alcance y el anexo define los contratos externos. Las decisiones técnicas se documentan con sus motivos y deben poder explicarse durante la defensa.

## Persistencia del catálogo

Se implementa una primera migración de producción con categorías, profesionales, horarios y una fila de estado de sincronización. Se conservan IDs externos y bajas lógicas. Las referencias se protegen con claves foráneas, los instantes se guardan en UTC y los horarios como hora local. La versión inicial es NULL hasta aplicar una copia completa. Los repositorios JPA permanecen en infraestructura y no se exponen por HTTP. Ver [modelo de datos](modelo-datos.md) para reglas, pruebas y límites.

## Modelos de dominio

Categorías, profesionales y horarios se representan con records inmutables en `catalog.domain.model`. Se usan únicamente tipos del JDK y reglas equivalentes al esquema existente. Un mapper explícito en infraestructura traduce entre modelos y entidades JPA; el dominio no conoce JPA. Esta separación prepara la futura aplicación de snapshots sin adelantar clientes externos ni endpoints.
