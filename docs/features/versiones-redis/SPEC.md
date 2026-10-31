# Consultar versiones del catálogo en Redis

## Contrato

El puerto interno `CatalogVersionSource.read()` devuelve `RemoteCatalogVersions` con `currentVersion` y `oldestAvailableVersion`. Ambos son enteros no negativos; la versión más antigua no puede superar la actual. Cero es válido.

El adaptador Lettuce lee `catedra:sync:current-version` y `catedra:sync:oldest-available-version` en un único MGET. Las claves faltantes, tipos distintos de String, valores no decimales, desbordamientos o rangos inconsistentes producen `CatalogVersionReadException`. Los errores de conexión y autenticación también se traducen a un mensaje genérico sin causas que expongan credenciales.

## Configuración

Desactivado por defecto. Para crear el lector, configurar `CATEDRA_REDIS_ENABLED=true`, `CATEDRA_REDIS_HOST`, `CATEDRA_REDIS_PORT`, `CATEDRA_REDIS_USERNAME`, `CATEDRA_REDIS_PASSWORD` y opcionalmente `CATEDRA_REDIS_TIMEOUT` (5s). El tiempo límite se aplica a conexión y comandos. El cliente se cierra con el contexto Spring; cada lectura cierra su conexión. No se reconecta automáticamente.

Las credenciales se cargan mediante variables de entorno. `.env.example` solo contiene campos vacíos. Spring no lee `.env` automáticamente. La creación del cliente no conecta al servidor.

## Alcance

Es un adaptador interno, sin endpoint ni tarea programada. Habilitarlo no dispara consultas. No escribe en Redis ni en MySQL, no obtiene cambios incrementales y no decide todavía si hace falta un snapshot. El estado HTTP existente sigue mostrando únicamente la última carga local.

Se utiliza Lettuce directamente, sin activar autoconfiguración ni health de Redis: el arranque normal continúa sin depender del servidor de cátedra. Las pruebas usan Redis temporal y credenciales ficticias, sin contactar al servidor docente.

## Referencias

Contrato de la cátedra: `INTEGRATION_REFERENCE-v2.md`, versiones de sincronización.

- [MGET: lectura atómica de varias claves](https://redis.io/docs/latest/commands/mget/).
- [Conexiones y autenticación con Lettuce](https://redis.github.io/lettuce/user-guide/connecting-redis/).
