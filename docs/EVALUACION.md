<!-- evaluacion:inicio -->
# Evaluación — mmachinea-pr2/turnos-catalog-service

> Documento **generado automáticamente** por la cátedra de Programación II (UM): RepoSense + análisis de calidad de **7 dimensiones**. **No editar a mano**: se regenera en cada evaluación.
> Rama evaluada: `main`. Análisis **heurístico** (regex + métricas) + build: es una criba para orientar la revisión, **no una nota**. La **documentación** del repositorio no se evalúa.
> Generado: 2026-11-20.

## 1. Veredicto

| Aspecto | Resultado |
|---|---|
| Índice de calidad | **75/100** |
| Tipo de repositorio | `backend` |
| Build (`mvnw verify` / `gradlew check`) | OK |
| Código (LOC) | 5,572 |
| Archivos de código | 136 |
| Clases | 140 |
| Código dentro de clases (%POO) | 75% |
| Funciones fuera de clases | 0 |
| Duplicación (puerta) | 2.0% |
| Duplicación lógica | 2.1% |
| Duplicación de boilerplate | 1.0% |
| Cobertura | 100% |
| Tope duro aplicado a D1 | `POO_BAJO` → D1 ≤ 60 |

> Este informe **describe** lo que el análisis midió con los criterios ya establecidos de la materia. No es una nota.

## 2. Semáforo de aprobación

Criterios de la cátedra (`CRITERIOS-CALIDAD.md`): **Aprobar** se juega con el perfil `estandar`; **destacar**, con el `exigente` (meta de excelencia).

| Semáforo | Resultado |
|---|---|
| Aprobar (`estandar`) | **cumple** — 8/11 |
| Destacar (`exigente`) | **no cumple** — 5/11 |

**Para destacar, falta cumplir:** SOLID; POO; Duplicación

## 3. Autoría (RepoSense)

| Autor | Commits | Código (LOC) | Tests (LOC) | Docs (LOC) | Días activos | % del código |
|---|---:|---:|---:|---:|---:|---:|
| pilarmachinea | 37 | 2,489 | 3,694 | 2,145 | 16 | 100% |

> _El LOC de autoría lo calcula RepoSense sobre el historial y puede diferir del LOC de código del análisis (que mira el estado de `main`)._

## 4. Dimensiones evaluadas

Cada dimensión indica **qué mide**, **por qué importa** y **tu resultado**. Cuando el puntaje queda por debajo de la referencia, se agregan preguntas para **investigar la causa** (no hay recetas).

| Dimensión | Peso | Puntaje |
|:--|--:|--:|
| **D1** Diseño OO, SOLID y patrones (20%) | 20% | 4 |
| **D2** Arquitectura hexagonal — backend (15%) | 15% | 100 |
| **D3** Frontend KMP: MVVM y buenas prácticas (10%) | 10% | N/A |
| **D4** Integración y contrato (10%) | 10% | 70 |
| **D5** Seguridad y robustez (10%) | 10% | 100 |
| **D6** Pruebas y cobertura (20%) | 20% | 100 |
| **D7** Proceso y entrega (15%) | 15% | 100 |

### D1. Diseño OO, SOLID y patrones (20%)

- **Qué mide:** Métricas orientadas a objetos (clases, métodos, campos, cohesión, acoplamiento, herencia) y proxies de SOLID, más patrones de diseño.
- **Por qué importa:** El eje de la materia es el buen uso de la POO y su arquitectura: un diseño con clases claras y poco acopladas es más fácil de entender, probar y cambiar.
- **Tu resultado:** **4/100**
- **Para investigar la causa:**
    - ¿Se pensó el modelo de clases antes de codificar o se agrupó después?
    - ¿Qué clases concentran muchas responsabilidades a la vez?
    - ¿Los colaboradores llegan como parámetros o se crean por dentro?

### D2. Arquitectura hexagonal — backend (15%)

- **Qué mide:** Separación en capas (dominio, puertos de entrada/salida, adaptadores) y dirección de las dependencias del backend.
- **Por qué importa:** Aislar el dominio de la infraestructura permite cambiar la base de datos, el framework o el transporte sin tocar las reglas de negocio.
- **Tu resultado:** **100/100**

### D4. Integración y contrato (10%)

- **Qué mide:** Cumplimiento del contrato de integración de la cátedra (formato de errores, paginación, autenticación, endpoints esperados).
- **Por qué importa:** Los servicios deben poder integrarse entre sí y con la app sin sorpresas; el contrato es lo que hacen cumplir las pruebas de integración.
- **Tu resultado:** **70/100**

### D5. Seguridad y robustez (10%)

- **Qué mide:** Credenciales embebidas, consultas SQL por concatenación, y presencia de config de entorno; aislamiento de secretos.
- **Por qué importa:** Un secreto en el repositorio o una consulta construida por concatenación son riesgos concretos, no hipotéticos.
- **Tu resultado:** **100/100**

### D6. Pruebas y cobertura (20%)

- **Qué mide:** Cantidad y resultado de pruebas y cobertura de línea (JaCoCo/Kover, Surefire/Failsafe) cuando hay build.
- **Por qué importa:** Las pruebas son la red de seguridad para refactorizar e integrar; la cobertura acota las zonas sin verificar.
- **Tu resultado:** **100/100**

### D7. Proceso y entrega (15%)

- **Qué mide:** Artefactos verificables de proceso/entrega: `compose`, `.env.example`, CI (`.github/workflows/`), `CODEOWNERS` y wrapper de build (`mvnw`/`gradlew`). **No** puntúa documentación.
- **Por qué importa:** Un proyecto que compila, se levanta y se valida solo es más fácil de revisar y de integrar en equipo.
- **Tu resultado:** **100/100**

## 5. Señales detectadas

El análisis encontró las siguientes señales, con el archivo y la línea donde se originan cuando están disponibles:

**Archivos con más señales**

| Archivo | Señales |
|---|---:|
| `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java` | 8 |
| `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogIncrementService.java` | 6 |
| `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/CatalogSnapshot.java` | 5 |
| `src/main/java/ar/edu/programacion2/catalog/security/SecurityConfiguration.java` | 3 |
| `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSource.java` | 3 |
| `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/startup/ManualFullCatalogSynchronization.java` | 3 |
| `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/WeeklyScheduleEntity.java` | 3 |
| `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/messaging/CatalogKafkaConfiguration.java` | 3 |
| `src/main/java/ar/edu/programacion2/catalog/security/JwtSettings.java` | 2 |
| `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogVersionSource.java` | 2 |

**DIP/concretos** (51)
- clase 'CatalogChangedEntities' muy acoplada (CBO=8) — `src/main/java/ar/edu/programacion2/catalog/catalog/application/model/CatalogChangedEntities.java:11`
- clase 'DetermineCatalogSynchronizationService' muy acoplada (CBO=10) — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/DetermineCatalogSynchronizationService.java:8`
- clase 'HandleCatalogUpdatedService' muy acoplada (CBO=11) — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/HandleCatalogUpdatedService.java:10`
- clase 'SynchronizeCatalogIncrementService' muy acoplada (CBO=22) — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogIncrementService.java:13`
- clase 'SynchronizeCatalogIncrementService' instancia 3 concretos (CBO=22) — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogIncrementService.java:13`
- clase 'SynchronizeCatalogService' muy acoplada (CBO=13) — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogService.java:10`
- clase 'TrackCatalogSynchronization' muy acoplada (CBO=11) — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/TrackCatalogSynchronization.java:10`
- clase 'CatalogSnapshot' muy acoplada (CBO=10) — `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/CatalogSnapshot.java:11`
- clase 'CatalogSnapshot' instancia 4 concretos (CBO=10) — `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/CatalogSnapshot.java:11`
- clase 'CatalogQueryConfiguration' muy acoplada (CBO=12) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/configuration/CatalogQueryConfiguration.java:19`
- clase 'CatalogQueryConfiguration' instancia 4 concretos (CBO=12) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/configuration/CatalogQueryConfiguration.java:19`
- clase 'CatalogRecoveryConfiguration' muy acoplada (CBO=10) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/configuration/CatalogRecoveryConfiguration.java:17`
- clase 'CatalogSynchronizationDecisionConfiguration' muy acoplada (CBO=9) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/configuration/CatalogSynchronizationDecisionConfiguration.java:18`
- clase 'CatalogKafkaConfiguration' muy acoplada (CBO=23) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/messaging/CatalogKafkaConfiguration.java:20`
- clase 'CatalogKafkaConfiguration' instancia 5 concretos (CBO=23) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/messaging/CatalogKafkaConfiguration.java:20`
- clase 'CatalogKafkaListener' muy acoplada (CBO=11) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/messaging/CatalogKafkaListener.java:8`
- clase 'CatalogKafkaListener' instancia 3 concretos (CBO=11) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/messaging/CatalogKafkaListener.java:8`
- clase 'CatalogUpdatedEventDecoder' muy acoplada (CBO=12) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/messaging/CatalogUpdatedEventDecoder.java:11`
- clase 'CatalogUpdatedEventDecoder' instancia 7 concretos (CBO=12) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/messaging/CatalogUpdatedEventDecoder.java:11`
- clase 'JdbcCatalogSynchronizationAttemptStore' muy acoplada (CBO=19) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/JdbcCatalogSynchronizationAttemptStore.java:14`
- clase 'JdbcProcessedCatalogEventStore' muy acoplada (CBO=17) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/JdbcProcessedCatalogEventStore.java:12`
- clase 'JdbcProcessedCatalogEventStore' instancia 3 concretos (CBO=17) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/JdbcProcessedCatalogEventStore.java:12`
- clase 'JpaCatalogIncrementStore' muy acoplada (CBO=12) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/JpaCatalogIncrementStore.java:12`
- clase 'JpaCatalogIncrementStore' instancia 3 concretos (CBO=12) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/JpaCatalogIncrementStore.java:12`
- clase 'JpaCatalogSnapshotStore' muy acoplada (CBO=11) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/JpaCatalogSnapshotStore.java:11`
- clase 'CatalogRedisConfiguration' muy acoplada (CBO=14) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/CatalogRedisConfiguration.java:18`
- clase 'CatalogRedisConfiguration' instancia 5 concretos (CBO=14) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/CatalogRedisConfiguration.java:18`
- clase 'RedisCatalogChangesSource' muy acoplada (CBO=16) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogChangesSource.java:16`
- clase 'RedisCatalogChangesSource' instancia 6 concretos (CBO=16) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogChangesSource.java:16`
- clase 'RedisCatalogEntitiesSource' muy acoplada (CBO=13) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSource.java:17`
- clase 'RedisCatalogEntitiesSource' instancia 4 concretos (CBO=13) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSource.java:17`
- clase 'RedisCatalogEntityParser' muy acoplada (CBO=16) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntityParser.java:13`
- clase 'RedisCatalogEntityParser' instancia 9 concretos (CBO=16) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntityParser.java:13`
- clase 'RedisCatalogVersionSource' muy acoplada (CBO=9) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogVersionSource.java:10`
- clase 'RedisCatalogVersionSource' instancia 3 concretos (CBO=9) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogVersionSource.java:10`
- clase 'CatalogRestConfiguration' muy acoplada (CBO=14) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/CatalogRestConfiguration.java:16`
- clase 'CatalogRestConfiguration' instancia 5 concretos (CBO=14) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/CatalogRestConfiguration.java:16`
- clase 'RestCatalogSnapshotSource' muy acoplada (CBO=15) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/RestCatalogSnapshotSource.java:12`
- clase 'RestCatalogSnapshotSource' instancia 3 concretos (CBO=15) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/RestCatalogSnapshotSource.java:12`
- clase 'CatalogSynchronizationSchedule' muy acoplada (CBO=10) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/scheduling/CatalogSynchronizationSchedule.java:11`
- clase 'CatalogSynchronizationSchedule' instancia 3 concretos (CBO=10) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/scheduling/CatalogSynchronizationSchedule.java:11`
- clase 'PeriodicCatalogSynchronization' muy acoplada (CBO=9) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/scheduling/PeriodicCatalogSynchronization.java:10`
- clase 'ManualCatalogSynchronization' muy acoplada (CBO=13) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/startup/ManualCatalogSynchronization.java:15`
- clase 'ManualFullCatalogSynchronization' muy acoplada (CBO=20) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/startup/ManualFullCatalogSynchronization.java:15`
- clase 'ManualFullCatalogSynchronization' instancia 5 concretos (CBO=20) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/startup/ManualFullCatalogSynchronization.java:15`
- clase 'ProfessionalController' muy acoplada (CBO=12) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/web/ProfessionalController.java:14`
- clase 'ProfessionalController' instancia 4 concretos (CBO=12) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/web/ProfessionalController.java:14`
- clase 'JwtSettings' muy acoplada (CBO=8) — `src/main/java/ar/edu/programacion2/catalog/security/JwtSettings.java:7`
- clase 'JwtSettings' instancia 3 concretos (CBO=8) — `src/main/java/ar/edu/programacion2/catalog/security/JwtSettings.java:7`
- clase 'SecurityConfiguration' muy acoplada (CBO=29) — `src/main/java/ar/edu/programacion2/catalog/security/SecurityConfiguration.java:24`
- clase 'SecurityConfiguration' instancia 6 concretos (CBO=29) — `src/main/java/ar/edu/programacion2/catalog/security/SecurityConfiguration.java:24`

**OCP/type-switch** (1)
- clase 'SecurityConfiguration' con 1 type-checks — `src/main/java/ar/edu/programacion2/catalog/security/SecurityConfiguration.java:24`

**SRP/responsabilidad** (36)
- método 'DetermineCatalogSynchronizationService.determine' con complejidad 6 — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/DetermineCatalogSynchronizationService.java:19`
- método 'HandleCatalogUpdatedService.handle' con complejidad 6 — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/HandleCatalogUpdatedService.java:23`
- método 'ListProfessionalsByCategoryService.list' con 5 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/ListProfessionalsByCategoryService.java:17`
- clase 'SynchronizeCatalogIncrementService' con baja cohesión (5%) — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogIncrementService.java:13`
- método 'SynchronizeCatalogIncrementService.SynchronizeCatalogIncrementService' con 5 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogIncrementService.java:20`
- método 'SynchronizeCatalogIncrementService.synchronize' de 35 líneas — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogIncrementService.java:31`
- método 'SynchronizeCatalogIncrementService.synchronize' con complejidad 10 — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogIncrementService.java:31`
- método 'SynchronizeCatalogService.synchronize' con complejidad 8 — `src/main/java/ar/edu/programacion2/catalog/catalog/application/service/SynchronizeCatalogService.java:20`
- método 'CatalogSnapshot.CatalogSnapshot' de 39 líneas — `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/CatalogSnapshot.java:11`
- método 'CatalogSnapshot.CatalogSnapshot' con 5 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/CatalogSnapshot.java:11`
- método 'CatalogSnapshot.CatalogSnapshot' con complejidad 8 — `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/CatalogSnapshot.java:11`
- método 'Professional.Professional' con 7 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/Professional.java:6`
- método 'ProfessionalCategory.ProfessionalCategory' con 6 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/ProfessionalCategory.java:6`
- método 'WeeklySchedule.WeeklySchedule' con 9 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/domain/model/WeeklySchedule.java:8`
- método 'CatalogSynchronizationDecisionConfiguration.synchronizeCatalogIncrement' con 5 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/configuration/CatalogSynchronizationDecisionConfiguration.java:20`
- método 'CatalogKafkaConfiguration.catalogKafkaContainer' de 34 líneas — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/messaging/CatalogKafkaConfiguration.java:22`
- clase 'CatalogSyncStateEntity' con baja cohesión (20%) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/CatalogSyncStateEntity.java:8`
- método 'JpaCatalogProfessionalReader.findByCategoryAndEnabled' con 5 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/JpaCatalogProfessionalReader.java:20`
- clase 'ProfessionalCategoryEntity' con baja cohesión (21%) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/ProfessionalCategoryEntity.java:8`
- método 'ProfessionalCategoryEntity.ProfessionalCategoryEntity' con 6 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/ProfessionalCategoryEntity.java:29`
- clase 'ProfessionalEntity' con baja cohesión (19%) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/ProfessionalEntity.java:8`
- método 'ProfessionalEntity.ProfessionalEntity' con 7 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/ProfessionalEntity.java:32`
- clase 'WeeklyScheduleEntity' grande (LOC=44, métodos=11, campos=9) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/WeeklyScheduleEntity.java:12`
- clase 'WeeklyScheduleEntity' con baja cohesión (16%) — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/WeeklyScheduleEntity.java:12`
- método 'WeeklyScheduleEntity.WeeklyScheduleEntity' con 9 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/persistence/WeeklyScheduleEntity.java:45`
- método 'RedisCatalogEntitiesSource.readEntities' con 7 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSource.java:42`
- método 'SnapshotResponse.SnapshotResponse' de 32 líneas — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java:12`
- método 'SnapshotResponse.SnapshotResponse' con 5 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java:12`
- método 'SnapshotResponse.CategoryResponse' con 6 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java:23`
- método 'CategoryResponse.CategoryResponse' con 6 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java:23`
- método 'SnapshotResponse.ProfessionalResponse' con 7 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java:31`
- método 'ProfessionalResponse.ProfessionalResponse' con 7 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java:31`
- método 'SnapshotResponse.ScheduleResponse' con 9 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java:39`
- método 'ScheduleResponse.ScheduleResponse' con 9 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/rest/SnapshotResponse.java:39`
- método 'ManualFullCatalogSynchronization.run' con complejidad 9 — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/startup/ManualFullCatalogSynchronization.java:32`
- método 'WeeklyScheduleResponse.WeeklyScheduleResponse' con 6 parámetros — `src/main/java/ar/edu/programacion2/catalog/catalog/infrastructure/web/WeeklyScheduleResponse.java:6`

**duplicación** (6)
- duplicación lógica: 2.0% · boilerplate: 1.0%
- bloque de 8 líneas repetido 4 veces en src/test/java/ar/edu/programacion2/catalog/ManualFullCatalogSynchronizationIntegrationTests.java:108, src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSourceTests.java:182, src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogChangesSourceTests.java:135 — `src/test/java/ar/edu/programacion2/catalog/ManualFullCatalogSynchronizationIntegrationTests.java:108`
- bloque de 8 líneas repetido 4 veces en src/test/java/ar/edu/programacion2/catalog/ManualFullCatalogSynchronizationIntegrationTests.java:110, src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSourceTests.java:184, src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogChangesSourceTests.java:137 — `src/test/java/ar/edu/programacion2/catalog/ManualFullCatalogSynchronizationIntegrationTests.java:110`
- bloque de 8 líneas repetido 4 veces en src/test/java/ar/edu/programacion2/catalog/catalog/application/service/DetermineCatalogSynchronizationServiceTests.java:15, src/test/java/ar/edu/programacion2/catalog/catalog/application/service/DetermineCatalogSynchronizationServiceTests.java:16, src/test/java/ar/edu/programacion2/catalog/catalog/application/service/DetermineCatalogSynchronizationServiceTests.java:17 — `src/test/java/ar/edu/programacion2/catalog/catalog/application/service/DetermineCatalogSynchronizationServiceTests.java:15`
- bloque de 8 líneas repetido 3 veces en src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSourceTests.java:179, src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogChangesSourceTests.java:132, src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogVersionSourceTests.java:94 — `src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSourceTests.java:179`
- bloque de 8 líneas repetido 3 veces en src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSourceTests.java:181, src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogChangesSourceTests.java:134, src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogVersionSourceTests.java:96 — `src/test/java/ar/edu/programacion2/catalog/catalog/infrastructure/redis/RedisCatalogEntitiesSourceTests.java:181`

## 6. Evolución por commit

Métricas calculadas sobre el estado de `main` en **cada commit** (28 commits con trabajo), del primero al último. La calidad usa el perfil `exigente`; el aporte de código lo mide RepoSense. El índice puede **bajar** al crecer el proyecto: se acumulan señales.

```mermaid
xychart-beta
    title "Índice de calidad a lo largo de los commits"
    x-axis ["c932474", "0ad0d4f", "dd71023", "fe2b2c4", "aa1f14b", "e57b2a8", "c41e1d1", "f0bd019", "0852e56", "67d002e", "dc8f155", "0ddc2fb", "8d522d7", "2a21653", "9393a20", "e07feba", "15227dc", "92ed8ce", "f868c6c", "46724b2", "4b25d10", "116afcf", "3bc34d4", "5a95117", "1afc235", "d06b414", "c245aa3", "e470c91"]
    y-axis "Índice" 0 --> 100
    line [0, 15, 34, 37, 42, 42, 47, 50, 54, 55, 56, 56, 55, 56, 61, 62, 63, 64, 64, 64, 65, 65, 64, 64, 64, 64, 64, 64]
```

| # | Fecha | Commit | Autor | LOC | Índice | %POO | Duplic. | Sueltas |
|---|---|---|---|---:|---:|---:|---:|---:|
| 1 | 2026-09-21 | `c932474` | pilarmachinea | 0 | 0 | 0% | 0.0% | 0 |
| 2 | 2026-09-22 | `0ad0d4f` | Ing. Daniel Quinteros | 0 | 15 | 0% | 0.0% | 0 |
| 3 | 2026-10-22 | `dd71023` | pilarmachinea | 311 | 34 | 78% | 0.0% | 0 |
| 4 | 2026-10-23 | `fe2b2c4` | pilarmachinea | 474 | 37 | 78% | 0.0% | 0 |
| 5 | 2026-10-23 | `aa1f14b` | pilarmachinea | 474 | 42 | 78% | 0.0% | 0 |
| 6 | 2026-10-27 | `e57b2a8` | pilarmachinea | 608 | 42 | 79% | 0.0% | 0 |
| 7 | 2026-10-27 | `c41e1d1` | pilarmachinea | 783 | 47 | 79% | 0.0% | 0 |
| 8 | 2026-10-27 | `f0bd019` | pilarmachinea | 1,052 | 50 | 81% | 0.0% | 0 |
| 9 | 2026-10-28 | `0852e56` | pilarmachinea | 1,205 | 54 | 80% | 0.0% | 0 |
| 10 | 2026-10-29 | `67d002e` | pilarmachinea | 1,535 | 55 | 76% | 0.0% | 0 |
| 11 | 2026-10-29 | `dc8f155` | pilarmachinea | 1,684 | 56 | 75% | 0.0% | 0 |
| 12 | 2026-10-31 | `0ddc2fb` | pilarmachinea | 1,732 | 56 | 75% | 0.0% | 0 |
| 13 | 2026-10-31 | `8d522d7` | pilarmachinea | 1,894 | 55 | 73% | 0.0% | 0 |
| 14 | 2026-10-31 | `2a21653` | pilarmachinea | 1,978 | 56 | 73% | 0.0% | 0 |
| 15 | 2026-10-31 | `9393a20` | pilarmachinea | 2,105 | 61 | 73% | 0.0% | 0 |
| 16 | 2026-10-31 | `e07feba` | pilarmachinea | 2,329 | 62 | 73% | 0.0% | 0 |
| 17 | 2026-10-31 | `15227dc` | pilarmachinea | 2,482 | 63 | 73% | 0.6% | 0 |
| 18 | 2026-10-31 | `92ed8ce` | pilarmachinea | 2,733 | 64 | 73% | 2.0% | 0 |
| 19 | 2026-11-01 | `f868c6c` | pilarmachinea | 3,066 | 64 | 74% | 2.5% | 0 |
| 20 | 2026-11-02 | `46724b2` | pilarmachinea | 3,288 | 64 | 74% | 2.4% | 0 |
| 21 | 2026-11-02 | `4b25d10` | pilarmachinea | 3,566 | 65 | 75% | 2.1% | 0 |
| 22 | 2026-11-02 | `116afcf` | pilarmachinea | 4,021 | 65 | 74% | 2.8% | 0 |
| 23 | 2026-11-03 | `3bc34d4` | pilarmachinea | 4,724 | 64 | 75% | 2.4% | 0 |
| 24 | 2026-11-03 | `5a95117` | pilarmachinea | 4,990 | 64 | 75% | 2.2% | 0 |
| 25 | 2026-11-11 | `1afc235` | pilarmachinea | 5,219 | 64 | 75% | 2.2% | 0 |
| 26 | 2026-11-11 | `d06b414` | pilarmachinea | 5,274 | 64 | 75% | 2.1% | 0 |
| 27 | 2026-11-17 | `c245aa3` | pilarmachinea | 5,327 | 64 | 75% | 2.1% | 0 |
| 28 | 2026-11-17 | `e470c91` | pilarmachinea | 5,572 | 64 | 75% | 2.0% | 0 |

---
_Informe generado por la cátedra. Las dimensiones y umbrales corresponden a `calidad.py` (perfil `exigente`); el semáforo, a `CRITERIOS-CALIDAD.md`. La documentación del repositorio no se evalúa. Ante dudas, revisá el archivo señalado._
<!-- evaluacion:fin -->
