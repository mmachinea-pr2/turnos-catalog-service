# Ejecución manual de la sincronización

## Objetivo

Permitir que una ejecución explícita de la aplicación obtenga y aplique el catálogo. El arranque habitual sigue sin consultar la cátedra ni reemplazar datos.

## Comportamiento

- La opción `catalog.synchronization.run-on-startup=true` habilita un ApplicationRunner que invoca una sola vez el caso de uso.
- Requiere habilitar también el cliente REST y configurar sus credenciales. Una configuración incompleta impide arrancar.
- Informa `snapshot applied` si se aplicó una versión o `version already covered, no changes` si ya estaba cubierta.
- Ante un fallo, el arranque termina con error; no reintenta ni muestra un mensaje de éxito. El error del runner no incorpora la causa ni el cuerpo remoto.
- La consulta precede a la transacción. El almacenamiento conserva sus garantías de rollback y comparación de versión.
- Con el modo no web, la ejecución no abre un puerto HTTP. No se crea un endpoint administrativo sin protección.

## Ejecutar localmente

Desde la raíz del repositorio, con ZeroTier conectado y `.env` configurado:

```bash
sdk env
set -a
. ./.env
set +a
unset MYSQL_ROOT_PASSWORD
docker compose up -d --wait mysql
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.main.web-application-type=none --catalog.catedra.enabled=true --catalog.synchronization.run-on-startup=true --catalog.catedra.read-timeout=30s"
```

El comando modifica la base MySQL de desarrollo indicada por el entorno: reemplaza la copia local si recibe una versión posterior, o aplica la primera carga si no hay versión. No altera la base de la cátedra. Ejecutar una sola instancia de esta carga manual y no guardar la opción de ejecución en la configuración habitual de IntelliJ.

La carga real se verificó con un máximo de lectura de 30s, incluido en el comando; no se modificó el valor predeterminado de 10s del cliente.

Los mensajes permiten distinguir aplicación, versión ya cubierta y error. No prueban que la versión sea la última disponible después de finalizar la petición: Kafka y la recuperación incremental se implementarán por separado.

## Pruebas

Un servidor HTTP local sirve un snapshot ficticio a la aplicación real en modo no web y esta lo guarda en MySQL temporal. Se comprueba primera carga, repetición sin cambios y HTTP 503 sin pérdida del estado anterior. Otras pruebas verifican que el arranque normal no requiere ni ejecuta este adaptador.

## Corrección encontrada en la carga real

El mapeo JDBC anterior convertía LocalTime mediante una zona horaria, aunque los horarios representan horas locales de atención. Con la JVM en America/Argentina/Mendoza y JDBC configurado en UTC, el intervalo ficticio 19:00–22:00 provocaba la violación de ck_schedule_interval. Se reprodujo el fallo en MySQL temporal antes de corregirlo.

Los campos startTime y endTime ahora usan `@JdbcTypeCode(SqlTypes.LOCAL_TIME)` para transferir LocalTime directamente por JDBC; los instantes de auditoría conservan su tratamiento UTC. No se altera la migración ni se elimina la restricción. La prueba de regresión comprueba las horas almacenadas mediante SQL y su lectura por JPA.

Referencia: [mapeo LOCAL_TIME de Hibernate](https://docs.hibernate.org/orm/current/javadocs/org/hibernate/type/SqlTypes.html#LOCAL_TIME).
