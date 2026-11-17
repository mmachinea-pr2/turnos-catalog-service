# Consultar el estado local de sincronización

## Contrato

`GET /api/catalog/synchronization/status` requiere JWT con ROLE_USER y devuelve HTTP 200 con estos campos:

- `initialized`: indica si se confirmó alguna versión en la copia local.
- `appliedVersion`: versión aplicada; null si todavía no se cargó el catálogo.
- `appliedAt`: instante UTC de aplicación en formato ISO-8601; null en el estado inicial.
- `lastAttempt`: inicio, fin, resultado y código de error del último intento (issue #57).

Antes de la primera carga:

```json
{"initialized": false, "appliedVersion": null, "appliedAt": null, "lastAttempt": {"startedAt": null, "completedAt": null, "result": "NEVER", "errorCode": null}}
```

Ejemplo ficticio de catálogo aplicado antes de empezar a registrar intentos:

```json
{"initialized": true, "appliedVersion": 0, "appliedAt": "2026-10-30T15:00:00Z", "lastAttempt": {"startedAt": null, "completedAt": null, "result": "NEVER", "errorCode": null}}
```

La versión cero es válida y se distingue de null. Un snapshot vacío aplicado también produce initialized=true. El instante corresponde a la aplicación local, no a generatedAt de la cátedra.

## Alcance y arquitectura

El controlador usa GetCatalogSynchronizationStatus, un servicio de aplicación y CatalogSynchronizationStatusReader. El adaptador JPA lee la fila única de catalog_sync_state en una transacción de solo lectura. Devuelve un modelo propio; no expone la entidad ni credenciales.

No consulta la cátedra, no dispara sincronización y no modifica datos. No compara contra la versión remota: initialized=true no garantiza que el catálogo esté actualizado. El objeto lastAttempt se lee de una tabla independiente. Ver [estados y límites del último intento](../ultimo-intento-sincronizacion/SPEC.md).

Si falta la fila obligatoria de estado, responde con error HTTP 500 en lugar de simular un catálogo sin inicializar. No sustituye al health técnico de Actuator.

## Uso local

```bash
curl -H "Authorization: Bearer $USER_JWT" http://localhost:8080/api/catalog/synchronization/status
```

## Pruebas

HTTP y MySQL temporal: estado inicial, versión cero, versión positiva, instante UTC con precisión conservada, ausencia de efectos de escritura y error ante una fila de estado faltante.
