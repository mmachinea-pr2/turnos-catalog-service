# Plan del dominio del catálogo

1. Definir modelos inmutables con los campos del contrato y las reglas presentes en persistencia.
2. Ubicar los mapeos a JPA en infraestructura, con dependencia hacia domain.
3. Probar horarios inválidos, datos obligatorios, bajas lógicas y conservación de valores al mapear.
4. Ejecutar todas las pruebas y documentar el resultado antes de publicar.

Rama local: `5-separar-modelos`, creada desde main integrado. El destino del PR es main, con revisión docente. Esta etapa corresponde a la issue #5.

Commit propuesto: `feat: separar modelos de dominio del catalogo`.
