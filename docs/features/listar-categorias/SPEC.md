# Listar categorías habilitadas

## Objetivo y contrato

`GET /api/catalog/categories` consulta exclusivamente la copia MySQL del catálogo. No llama a REST, Redis ni Kafka de la cátedra.

Devuelve HTTP 200 y un array JSON con `id`, `name` y `description` por categoría. La descripción puede ser null. Solo incluye categorías habilitadas, ordenadas por nombre ascendente según la colación de MySQL y, ante empate, por ID ascendente.

Ejemplo ficticio:

```json
[
  {"id": 1, "name": "Clínica", "description": "Atención general"}
]
```

Si la base está vacía o no hay categorías habilitadas, devuelve `[]`. Esta respuesta no distingue un catálogo sin sincronizar de un catálogo vacío; el estado de sincronización tendrá una consulta separada. No hay paginación ni filtros en este primer listado de categorías.

## Arquitectura

CategoryController convierte modelos a DTO de respuesta y utiliza el puerto de entrada ListEnabledCategories. El servicio depende de CatalogCategoryReader; JpaCatalogCategoryReader filtra y ordena en MySQL dentro de una transacción de solo lectura. No se exponen entidades JPA por HTTP. La configuración de consultas es independiente de la activación del cliente REST de la cátedra.

## Uso local

Arrancar el backend como indica el README y consultar:

```bash
curl http://localhost:8080/api/catalog/categories
```

La seguridad JWT todavía está pendiente: este endpoint no solicita autenticación en esta etapa de desarrollo. El servicio se mantiene en su configuración local habitual; no se considera terminada la protección de la API.

## Validación

Pruebas HTTP contra MySQL temporal: exclusión de bajas lógicas, orden estable con nombres iguales, descripción nula, contrato exacto de campos, base vacía y categorías únicamente deshabilitadas. No se requieren credenciales ni conectividad con la cátedra.
