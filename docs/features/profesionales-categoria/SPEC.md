# Consultar profesionales por categoría

## Contrato

`GET /api/catalog/professionals?categoryId=1` devuelve HTTP 200 con un array de profesionales habilitados de la categoría indicada. La categoría también debe estar habilitada. La consulta se realiza únicamente en MySQL local.

Cada resultado contiene `id`, `categoryId`, `firstName` y `lastName`. No se exponen entidades JPA ni campos de auditoría. Se ordena por apellido, nombre e ID ascendentes; los textos siguen la colación configurada en MySQL.

`categoryId` es obligatorio y debe ser un entero positivo representable como long. Valores ausentes, no numéricos, fuera de rango, cero o negativos producen HTTP 400. Una categoría inexistente, deshabilitada o sin profesionales habilitados devuelve HTTP 200 con `[]`.

Ejemplo ficticio:

```json
[
  {"id": 15, "categoryId": 1, "firstName": "Ana", "lastName": "Gómez"}
]
```

## Arquitectura

ProfessionalController → ListProfessionalsByCategory → CatalogProfessionalReader → JpaCatalogProfessionalReader. Los puertos y el servicio usan modelos de dominio sin dependencias de Spring. La consulta JPA une profesionales con categorías y aplica filtros y orden en una transacción de solo lectura.

## Alcance

Este incremento agrega el filtro por categoría. La [búsqueda por nombre](../buscar-profesionales/SPEC.md) amplía este contrato mediante un parámetro opcional. La paginación, consulta de horarios y disponibilidad se implementarán por separado. La protección JWT sigue pendiente; el endpoint está disponible sin autenticación en el entorno de desarrollo local. No se envía el JWT técnico de cátedra al cliente.

## Probar localmente

Con el backend iniciado y una copia cargada, obtener un ID desde `/api/catalog/categories` y consultar:

```bash
curl 'http://localhost:8080/api/catalog/professionals?categoryId=1'
```

## Validación

Pruebas HTTP con MySQL temporal: selección de categoría, exclusión de profesionales y categorías deshabilitados, desempate por nombre e ID, contrato exacto de campos, categoría inexistente o vacía y parámetros inválidos. Los datos son ficticios y no requieren conectividad con la cátedra.
