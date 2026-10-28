# Consulta REST del snapshot

## Objetivo

Obtener el catálogo de la cátedra mediante GET /api/synchronization/snapshot y devolver un CatalogSnapshot validado. Esta entrega no ejecuta la consulta al arrancar, no escribe en MySQL y no expone un endpoint público.

## Comportamiento

- Envía el JWT técnico en Authorization: Bearer y solicita JSON.
- Requiere HTTP 200 y un cuerpo completo. Rechaza versión, colecciones, fechas, estados habilitados y duración ausentes o nulos.
- Conserva IDs, fechas y bajas lógicas; admite horas HH:mm y HH:mm:ss.
- Tolera campos adicionales y aplica las validaciones del dominio, incluidas referencias y duplicados.
- Limita conexión y lectura; no realiza reintentos automáticos ni sigue redirecciones.
- Informa HTTP fallido, fallo de red o respuesta inválida sin propagar cuerpos remotos, credenciales ni causas de excepción sensibles.

## Configuración

Variables locales: CATEDRA_REST_ENABLED, CATEDRA_API_BASE_URL, CATEDRA_JWT, CATEDRA_CONNECT_TIMEOUT (5s), CATEDRA_READ_TIMEOUT (10s). Las direcciones y el JWT se completan en .env o en las variables del proceso. Spring no carga .env automáticamente; usar el procedimiento del README. La integración está desactivada por defecto. Activarla crea el cliente, pero no inicia una sincronización.

CatalogSnapshotSource es el puerto de salida. RestCatalogSnapshotSource utiliza RestClient y DTO propios del transporte, sin añadir dependencias de HTTP al dominio. El siguiente avance conectará este puerto con la persistencia mediante un caso de uso.

Referencia técnica: [RestClient de Spring](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html).
