# Eventos de catálogo y consumidor Kafka

La funcionalidad se desarrolla en varios commits dentro de una issue. El primer incremento prepara el contrato, el procesamiento y el registro durable de eventos. El segundo conecta el consumidor opcional y controla sus confirmaciones.

## Contrato de entrada

`CatalogUpdatedEventDecoder` acepta el JSON de `CatalogUpdated`, schemaVersion=1 y una message key igual a newVersion en texto decimal. Exige eventId no vacío, versión no negativa, occurredAt válido y source no vacío. Ignora campos adicionales, pero rechaza tipos incorrectos, campos ausentes, versiones de esquema incompatibles, claves inconsistentes y documentos JSON adicionales. No convierte strings numéricos.

El límite técnico de eventId es 255 bytes UTF-8, acorde a la clave de almacenamiento local; un valor mayor se rechaza, nunca se trunca. La fuente es texto, para admitir nuevos orígenes sin cambiar el contrato. El error de decodificación no incluye el payload ni su causa.

## Procesamiento

`HandleCatalogUpdated` usa eventId como clave funcional de idempotencia. Un evento ya registrado se devuelve como DUPLICATE sin consultar servicios externos. Reutilizar un eventId para otra versión produce CatalogEventConflictException.

Si la versión local ya cubre el evento, lo registra sin sincronizar. En caso contrario ejecuta `SynchronizeCatalog` una vez y vuelve a consultar la versión local. Solo marca PROCESSED si esta cubre newVersion. Un resultado aparentemente exitoso de sincronización no basta: un evento puede llegar antes que los datos. En ese caso devuelve RETRY_REQUIRED sin registrar el evento. También conserva los resultados de reintento o versión adelantada y propaga los errores.

El evento notifica, no contiene entidades ni modifica directamente la versión local. Los datos siguen obteniéndose mediante Redis o REST.

## Persistencia

La migración nueva `V2__processed_catalog_event.sql` agrega una tabla propia con event_id, new_version y processed_at UTC. No modifica V1. La clave binaria conserva diferencias de mayúsculas y espacios del identificador.

El registro toma el mismo bloqueo de estado que los snapshots e incrementos, comprueba de nuevo la versión y evita filas duplicadas ante escrituras concurrentes. Solo esa comprobación y registro usan una transacción corta; las consultas externas quedan fuera.

Guardar datos y registrar el evento son operaciones separadas. Si el proceso cae entre ambas, la repetición encuentra la versión cubierta y puede registrar el evento sin volver a aplicar el cambio. Dos manejadores simultáneos podrían consultar servicios externos, pero la persistencia de catálogo conserva sus controles de versión y el registro produce una sola fila. No se promete ejecución exactamente una vez.

El registro no se borra durante snapshots. Aún no hay política de purga: eliminar registros afectaría la deduplicación por eventId.

## Consumidor opcional

`CATEDRA_KAFKA_ENABLED=false` por defecto. Para activarlo, habilitar también REST y Redis y completar las variables propias de integración:

- `CATEDRA_KAFKA_BOOTSTRAP_SERVERS`: brokers de cátedra.
- `CATEDRA_KAFKA_CATALOG_TOPIC`: topic exacto entregado al grupo.
- `CATEDRA_KAFKA_CONSUMER_GROUP_ID`: grupo propio; debe conservarse entre reinicios.

Se utiliza un contenedor Spring Kafka con un consumidor y deserializadores String. No se crea ningún topic ni se publica en Kafka. La confirmación automática está desactivada; AckMode.RECORD confirma después de que el manejador devuelva PROCESSED o DUPLICATE. max.poll.records=1 limita cada lectura a un evento y auto.offset.reset=earliest permite recuperar eventos retenidos si el grupo no tiene offset inicial. No reinicia los offsets ya confirmados.

Ante un mensaje inválido, un error de base o red, RETRY_REQUIRED o LOCAL_VERSION_AHEAD, el listener lanza un error seguro y CommonContainerStoppingErrorHandler detiene el consumo. Los mensajes exitosos previos quedan confirmados y el fallido no se descarta. Tras corregir la causa, reiniciar la aplicación con el mismo grupo vuelve a entregar el pendiente. No hay reintentos automáticos, DLT ni omisión de mensajes inválidos: un mensaje que sigue siendo inválido volverá a detener el consumidor y requiere revisión del contrato con su emisor.

El indicador de salud Kafka solo se registra cuando el consumidor está habilitado. Informa DOWN si está detenido o sin particiones asignadas. Comprueba ese estado local, no la actualidad de los datos ni una conexión instantánea con el broker. El health público sigue ocultando detalles internos.

## Ejecución

Con ZeroTier conectado y `.env` completo, desde la terminal del repositorio:

```bash
sdk env
set -a
. ./.env
set +a
unset MYSQL_ROOT_PASSWORD
docker compose up -d --wait mysql
./mvnw spring-boot:run -Dspring-boot.run.arguments="--catalog.catedra.enabled=true --catalog.catedra.redis.enabled=true --catalog.catedra.kafka.enabled=true"
```

Es una ejecución continua, compatible con las consultas HTTP. No debe combinarse con ninguno de los dos comandos de sincronización manual; una configuración incompatible falla antes del consumo. Los comandos manuales documentados desactivan Kafka explícitamente.

Para ejecutar el backend con Compose, configurar en el entorno local `CATEDRA_REST_ENABLED=true`, `CATEDRA_REDIS_ENABLED=true` y `CATEDRA_KAFKA_ENABLED=true`, además de los datos de integración, y usar `docker compose up --build -d`. Compose transmite esas variables al contenedor sin incorporarlas a la imagen. Usar solo una modalidad de ejecución del backend a la vez. El acceso desde Docker a la red ZeroTier también debe estar disponible.

## Referencias técnicas

- [Spring Kafka: detención y confirmaciones ante errores](https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html#container-stopping-error-handlers).
- [Testcontainers: broker Kafka temporal](https://java.testcontainers.org/modules/kafka/).

## Pruebas

Validación del mensaje y clave, campos adicionales, duplicados, eventos antiguos, eventos adelantados respecto de los datos y fallos sin registro. MySQL temporal: migración, persistencia, identidad sensible a mayúsculas, colisiones de versión, concurrencia y procesamiento con guardado real fuera de la transacción de red.


Pruebas del segundo incremento con un broker Kafka temporal: confirmación del evento exitoso, ausencia de confirmación del fallido, reentrega tras reiniciar el contenedor consumidor, continuación con el mismo grupo tras recrear el contexto y confirmación de duplicados. Mensajes inválidos detienen el consumidor sin llegar al manejador. Esta prueba usa un manejador controlado; la persistencia e idempotencia reales se verifican por separado con MySQL. No se comprobó la publicación ni el consumo en el servidor docente.
