# Consultar el estado local de sincronización

## Contrato

`GET /api/catalog/synchronization/status` devuelve HTTP 200 con tres campos:

- `initialized`: indica si se confirmó alguna versión en la copia local.
- `appliedVersion`: versión aplicada; null si todavía no se cargó el catálogo.
- `appliedAt`: instante UTC de aplicación en formato ISO-8601; null en el estado inicial.

Antes de la primera carga:

```json
{"initialized": false, "appliedVersion": null, "appliedAt": null}
```

Ejemplo ficticio de carga aplicada:

```json
{"initialized": true, "appliedVersion": 0, "appliedAt": "2026-10-30T15:00:00Z"}
```

La versión cero es válida y se distingue de null. Un snapshot vacío aplicado también produce initialized=true. El instante corresponde a la aplicación local, no a generatedAt de la cátedra.

## Alcance y arquitectura

El controlador usa GetCatalogSynchronizationStatus, un servicio de aplicación y CatalogSynchronizationStatusReader. El adaptador JPA lee la fila única de catalog_sync_state en una transacción de solo lectura. Devuelve un modelo propio; no expone la entidad ni credenciales.

No consulta la cátedra, no dispara sincronización y no modifica datos. No compara contra la versión remota: initialized=true no garantiza que el catálogo esté actualizado. Tampoco informa el último error o una sincronización en curso; esas métricas quedan para próximos avances. La seguridad JWT sigue pendiente.

Si falta la fila obligatoria de estado, responde con error HTTP 500 en lugar de simular un catálogo sin inicializar. No sustituye al health técnico de Actuator.

## Uso local

```bash
curl http://localhost:8080/api/catalog/synchronization/status
```

## Pruebas

HTTP y MySQL temporal: estado inicial, versión cero, versión positiva, instante UTC con precisión conservada, ausencia de efectos de escritura y error ante una fila de estado faltante.
