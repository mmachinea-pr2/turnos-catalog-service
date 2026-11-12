# Filtro por estado habilitado de profesionales

Issue #55. Endpoint protegido con JWT y ROLE_USER:

`GET /api/catalog/professionals?categoryId=101&enabled=false&name=Ana&dayOfWeek=FRIDAY&page=0&size=20`

## Comportamiento

- `enabled=true` selecciona profesionales habilitados y es el valor por defecto.
- `enabled=false` selecciona profesionales deshabilitados.
- La categoría debe continuar habilitada: el filtro se aplica al estado del
  profesional, no al de su categoría.
- La conversión booleana es la estándar de Spring MVC. Usar `true` y `false`
  como valores del contrato; un valor no convertible devuelve 400.
- Categoría, nombre, estado y día se filtran antes de paginar. El total de
  `X-Total-Count` cuenta profesionales y no agendas. Se conserva el orden
  apellido, nombre e ID y el cuerpo de respuesta anterior.
- Sin `dayOfWeek`, los profesionales pueden no tener agendas habilitadas.
  Con ese parámetro, se exige una agenda cuyo propio `enabled` sea true para
  ese día. Esto también puede encontrarse en un profesional deshabilitado;
  no significa que sea reservable.

## Protección de nuevas reservas

Consultar un profesional deshabilitado no cambia su estado ni publica su
agenda operativa. `/api/catalog/professionals/{id}/schedules` conserva la
exigencia de profesional, categoría y agenda habilitados. Booking utiliza esa
operación para calcular disponibilidad y validar nuevos holds; no usa la lista
de búsqueda para autorizar una reserva. No se modifican booking, sus permisos,
las migraciones ni los datos de desarrollo.

## Implementación y pruebas

La consulta JPA utiliza el estado solicitado y EXISTS para las agendas, evitando
duplicados. Los puertos internos se denominan `findByCategoryAndEnabled` para
expresar que ya no consultan únicamente profesionales habilitados.

Pruebas HTTP con JWT y MySQL mediante Testcontainers: compatibilidad entre filtro
omitido y true; selección de false; categoría oculta; combinación con nombre y
día; varias agendas; páginas, totales y página vacía; agenda operativa vacía de
un profesional deshabilitado; rechazo de entradas no convertibles.

Ejecutar con Java 25 y Docker: `./mvnw -B -ntp verify`.
