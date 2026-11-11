# Seguridad JWT de catálogo

Issue #51. Milestone: Seguridad e integración de backends.

## Contrato

Booking registra usuarios y emite JWT. Catálogo actúa como resource server y no
implementa registro, login ni acceso a la base de usuarios. Booking propaga el token
del usuario al consultar catálogo; el cliente también puede usar ese mismo token.

Las consultas GET bajo `/api/catalog/**` requieren `Authorization: Bearer <JWT>` y
el permiso exacto `ROLE_USER` en el claim `auth`, una cadena de permisos separados
por espacios. Incluye categorías, profesionales, agendas y estado de sincronización.
El único acceso público inicial es GET `/actuator/health`, sin detalles internos.
Los otros endpoints y métodos quedan denegados. No se habilita autenticación por
cookies, sesión, Basic ni formulario; no se acepta access_token en query strings.
Se conserva la protección CSRF de Spring Security y las cabeceras no-store.

Respuestas: 401 para token ausente o inválido; 403 para token válido sin permisos.
Un token válido sigue recibiendo los errores funcionales habituales de cada consulta.
El despacho interno ERROR puede mostrar esos errores; no habilita acceso público
directo a `/error` ni a los controladores protegidos.

## Validaciones

- Firma HMAC HS256; no se aceptan algoritmos alternativos ni tokens sin firma.
- Emisor `turnos-booking` y audiencia `turnos-backends`, configurables en ambos servicios.
- Subject UUID canónico, iat y exp obligatorios; exp posterior a iat.
- iat no futuro, exp vigente y nbf respetado cuando está presente, sin tolerancia temporal.
- Claim auth ausente implica ausencia de permisos; un tipo distinto de cadena es inválido.

La configuración del servidor web falla al arrancar si la clave falta, no es Base64,
contiene menos de 32 bytes decodificados, o el emisor o la audiencia están vacíos.
No hay clave predeterminada ni una opción de arranque web sin autenticación.
Las claves de pruebas son exclusivas de los tests. Las sincronizaciones no web no
instancian la configuración HTTP y no necesitan esta clave.

## Configuración local y Docker

En el `.env` local de catálogo agregar `CATALOG_JWT_SECRET` con **el mismo valor**
que `BOOKING_JWT_SECRET` de booking. Es una clave Base64, no un JWT emitido ni la
credencial técnica de cátedra. No publicar ninguno de esos valores.

| Catálogo | Valor de booking que debe coincidir |
| --- | --- |
| CATALOG_JWT_SECRET | BOOKING_JWT_SECRET |
| CATALOG_JWT_ISSUER | BOOKING_JWT_ISSUER; predeterminado turnos-booking |
| CATALOG_JWT_AUDIENCE | BOOKING_JWT_AUDIENCE; predeterminado turnos-backends |

Compose exige la clave y la pasa al contenedor. En IntelliJ añadir las mismas variables
a Run → Edit Configurations → Environment variables junto a las de MySQL.
Spring no carga `.env` automáticamente al ejecutar desde IntelliJ. No modificar ni
regenerar la clave de booking solo para configurar catálogo: copiar su valor local.

Para consultar, iniciar sesión en booking y enviar su id_token en la pestaña
Authorization → Bearer Token de Postman contra catálogo. La salud puede consultarse
sin token. No pegar credenciales en capturas, issues o documentación.

## Decisiones y límites

Se mantiene HS256 por compatibilidad con booking. Ambos servicios comparten una
clave, por lo que cualquiera que la posea podría firmar tokens. Catálogo no ofrece
un emisor ni endpoint de login. Una evolución a firma asimétrica permitiría que solo
booking conserve la clave privada; no se implementa en esta entrega.

La revocación de usuarios no se consulta desde catálogo. Un JWT ya emitido conserva
su acceso a catálogo hasta expirar (booking emite por defecto tokens de 30 minutos).
Booking comprueba además la cuenta activa en sus operaciones. No se comparte la
base de datos de usuarios. Los relojes de ambos servicios deben estar sincronizados.

No se habilita CORS para orígenes de navegador. Android y la comunicación HTTP
entre backends no lo necesitan. La validación JWT no cifra HTTP: fuera del entorno
local/ZeroTier debe configurarse HTTPS en el despliegue.

## Pruebas

Pruebas con JWT firmados: token de booking compatible, firmas y algoritmos inválidos,
claims ausentes, vencimiento, nbf/iat futuros, permisos, endpoints cerrados y salud.
Las pruebas funcionales existentes de catálogo ahora envían JWT válidos y siguen
probando MySQL y migraciones reales. Se prueba que el modo no web no requiere clave.
La validación conjunta de las dos aplicaciones desplegadas queda para integración.
