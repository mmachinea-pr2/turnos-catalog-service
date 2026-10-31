# Buscar profesionales por nombre

## Contrato

Se amplía `GET /api/catalog/professionals?categoryId=1` con el parámetro opcional `name`:

```text
GET /api/catalog/professionals?categoryId=1&name=ana
```

Busca una coincidencia parcial literal sobre el nombre completo (`firstName + espacio + lastName`). Permite buscar fragmentos del nombre, apellido o ambos en ese orden. No distingue mayúsculas; el tratamiento de tildes sigue la colación de MySQL. El servicio elimina espacios al principio y al final y reduce espacios consecutivos de la consulta a uno.

Si `name` falta, está vacío o solo contiene espacios, devuelve el mismo listado que antes. Si no coincide, devuelve HTTP 200 con `[]`. `categoryId` sigue siendo obligatorio y positivo.

Se mantienen el filtro por categoría, las exclusiones de categorías y profesionales deshabilitados, el orden por apellido/nombre/ID y el DTO existente. La [paginación](../paginar-profesionales/SPEC.md) se aplica después de filtrar y mantiene este DTO. JWT continúa pendiente.

## Implementación

El servicio normaliza el texto usando Locale.ROOT. El adaptador JPA utiliza LOCATE y un parámetro enlazado: `%` y `_` son caracteres literales, y el texto no se concatena a la consulta SQL. El filtrado ocurre en MySQL, no después de cargar todos los profesionales en memoria. No se consulta la cátedra.

## Prueba manual

Con el backend iniciado y datos locales:

```bash
curl --get 'http://localhost:8080/api/catalog/professionals' --data-urlencode 'categoryId=1' --data-urlencode 'name=ana'
```

La coincidencia depende del contenido de la categoría elegida. Usar un ID y un nombre disponibles en la copia local.

## Pruebas

Integración HTTP con MySQL temporal: nombre parcial, apellido, nombre completo con espacios y mayúsculas, consulta vacía, sin coincidencias y caracteres especiales. Se verifica que la búsqueda no incluya otras categorías ni bajas lógicas. Las pruebas anteriores mantienen cubierto el contrato sin name y los errores de categoryId.
