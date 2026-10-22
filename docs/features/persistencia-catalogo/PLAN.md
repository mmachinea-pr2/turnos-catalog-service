# Plan de persistencia del catálogo

## Pasos y commits propuestos

1. Diseño: revisar SPEC, modelo de datos, este plan y tareas. Commit de documentación independiente.
2. Implementación: incorporar migración, entidades, repositorios y pruebas coherentes en un incremento que compile y pase las pruebas. Actualizar README con el alcance implementado.

No se divide la implementación de forma que un commit deje una migración incompatible con las pruebas. Cada commit debe ser revisable y expresar un avance concreto.

## Separación de responsabilidades

Las dependencias JPA quedan en infraestructura. La futura aplicación de snapshots utilizará puertos y casos de uso; el dominio no importará Spring o JPA. No se crean carpetas vacías para representar capas todavía no implementadas.

## Validación

Ejecutar `./mvnw -B -ntp clean verify` con Java 25 y Docker. Verificar lectura y escritura, bajas lógicas, claves foráneas, restricciones horarias, estado inicial y rollback de datos y versión. Confirmar que el JAR contiene la migración de producción y no la antigua migración de prueba.

## Publicación

La rama de trabajo es `codex/catalogo-persistencia`. El PR se dirigirá a `main` para revisión del profesor, según el flujo actualizado. No se hace push directo a main.

Esta rama parte de `1-preparacion`; antes de abrir el PR hay que comprobar si esa primera entrega ya fue integrada. Si no lo fue, el diff incluirá también su base técnica y deberá acordarse cómo revisar esa dependencia.
