# Etapa de persistencia del catálogo

## Objetivo

Preparar el esquema MySQL y los repositorios internos donde se guardarán categorías, profesionales, horarios y versión local. No incluye frontend ni cambios en el backend de reservas.

## Organización propuesta en GitHub

- Milestone: M2 — Persistencia y sincronización del catálogo.
- Issue: #3 — Implementar la persistencia del catálogo.
- Tipo: Task.
- Etiquetas: enhancement y documentation, si están disponibles.
- Rama local: 3-implemetar-la-persistencia-del-catalgo.
- Destino final del PR: main, con revisión del profesor; sin push directo a main.

La issue #3 identifica esta etapa. No se fija fecha límite sin acuerdo docente.

La implementación parte de la base técnica de `1-preparacion`. La rama actual incorpora el informe docente de `main` y el commit previo de diseño. El PR se dirigirá a `main`; mientras la primera entrega no esté integrada, el diff también incluirá esa base técnica. La publicación se realiza por PR, sin push directo a main.

## Criterios de aceptación

- Migración de producción con tablas y claves foráneas del catálogo.
- Entidades JPA compatibles con el esquema y repositorios internos.
- Conservación de IDs externos y entidades deshabilitadas.
- Versión inicial no sincronizada diferenciada de la versión cero.
- Pruebas de escritura y lectura, horarios inválidos, referencias inexistentes y rollback de datos y versión.
- Documentación del modelo, decisiones y límites.

## Próxima implementación

Caso de uso de aplicación transaccional de un snapshot completo, validación de sus referencias y versión, y posteriormente cliente REST con autenticación técnica. La exposición de endpoints de negocio debe incorporar la seguridad correspondiente.

## Verificación reproducible

Ejecutar `sdk env` y `./mvnw -B -ntp clean verify` con Docker disponible. `clean` elimina recursos compilados de la etapa anterior, incluida la migración de prueba retirada. Los contenedores temporales se eliminan al terminar y no usan la base de desarrollo.

Validación realizada el 22 de octubre de 2026: BUILD SUCCESS; seis pruebas ejecutadas, sin fallos, errores ni omisiones. El JAR contiene la migración de producción y no contiene setup_probe. La base de desarrollo no fue modificada en esta etapa.
