# Filtro por disponibilidad de agenda

Issue #53. Consulta local protegida con el JWT de usuario de booking.

`GET /api/catalog/professionals?categoryId=101&dayOfWeek=FRIDAY&name=Elena&page=0&size=20`

El parámetro opcional `dayOfWeek` admite MONDAY, TUESDAY, WEDNESDAY, THURSDAY,
FRIDAY, SATURDAY y SUNDAY. Un valor desconocido devuelve HTTP 400. Omitirlo
conserva la búsqueda anterior, incluyendo profesionales sin agenda habilitada.

Con el filtro se devuelven profesionales del estado seleccionado por `enabled`
(true por defecto), de una categoría habilitada y con al menos una agenda
habilitada para ese día. La selección del estado se incorporó en la issue #55;
un profesional deshabilitado no tiene agenda operativa para nuevas reservas. Se combina
con el nombre y la categoría antes de paginar. Se conserva el orden por apellido,
nombre e ID y el encabezado `X-Total-Count` cuenta profesionales, no agendas.
El cuerpo y los DTO existentes no cambian. JWT ausente o inválido devuelve 401;
un token sin ROLE_USER devuelve 403.

La consulta utiliza EXISTS sobre las agendas locales, evitando multiplicar las
filas de un profesional con varios horarios. No consulta Redis ni cátedra y no
requiere migraciones. Las bajas lógicas se reflejan cuando se aplica la
sincronización correspondiente.

Este filtro indica atención semanal, no cupos libres en una fecha. Para conocer
turnos disponibles, booking combina la agenda con las ocupaciones de cátedra.
Una caída de cátedra no impide buscar en la copia local previamente cargada,
pero esa copia puede estar desactualizada.

## Validación

Pruebas HTTP con JWT y MySQL real mediante Testcontainers: múltiples agendas,
agenda deshabilitada, profesional sin agenda, bajas de profesional y categoría,
combinación con nombre, día sin resultados, páginas y total sin duplicados,
compatibilidad sin filtro y rechazo de días inválidos.

Ejecutar con Java 25 y Docker: `./mvnw -B -ntp verify`.
