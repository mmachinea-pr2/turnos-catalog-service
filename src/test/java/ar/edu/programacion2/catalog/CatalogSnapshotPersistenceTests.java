package ar.edu.programacion2.catalog;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotStore;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogIncrementStore;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogIncrementRejectedException;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogChangedEntities;
import ar.edu.programacion2.catalog.catalog.domain.model.*;
import ar.edu.programacion2.catalog.catalog.infrastructure.persistence.*;
import ar.edu.programacion2.catalog.catalog.application.service.SynchronizeCatalogSnapshotService;
import ar.edu.programacion2.catalog.catalog.application.service.SynchronizeCatalogService;
import ar.edu.programacion2.catalog.catalog.application.service.SynchronizeCatalogIncrementService;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationStatus;
import ar.edu.programacion2.catalog.catalog.application.model.RemoteCatalogVersions;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogIncrement;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogChangesSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogEntitiesSource;
import static org.mockito.Mockito.*;
import ar.edu.programacion2.catalog.catalog.application.port.out.SnapshotFetchException;
import ar.edu.programacion2.catalog.catalog.infrastructure.rest.RestCatalogSnapshotSource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogSnapshotPersistenceTests {
    @Container
    static final org.testcontainers.mysql.MySQLContainer MYSQL = new org.testcontainers.mysql.MySQLContainer("mysql:8.4.8");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", MYSQL::getJdbcUrl);
        properties.add("spring.datasource.username", MYSQL::getUsername);
        properties.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired ar.edu.programacion2.catalog.catalog.application.port.out.ProcessedCatalogEventStore processedEvents;
    @Autowired CatalogSnapshotStore store;
    @Autowired CatalogIncrementStore increments;
    @Autowired ProfessionalCategoryRepository categories;
    @Autowired ProfessionalRepository professionals;
    @Autowired WeeklyScheduleRepository schedules;
    @Autowired CatalogSyncStateRepository states;
    @Autowired JdbcTemplate jdbc;
    private static final Instant SOURCE_TIME = Instant.parse("2026-09-10T13:40:00Z");

    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM processed_catalog_event");
        schedules.deleteAllInBatch();
        professionals.deleteAllInBatch();
        categories.deleteAllInBatch();
        jdbc.update("DELETE FROM catalog_sync_state");
        jdbc.update("INSERT INTO catalog_sync_state VALUES (1, NULL, NULL)");
    }

    @Test
    void appliesVersionZeroWithSourceIdsDisabledRecordsAndLocalTimes() {
        assertThat(store.apply(snapshot(0, "Clínica"))).isTrue();
        assertThat(categories.findById(101L).orElseThrow().getEnabled()).isFalse();
        assertThat(professionals.findById(201L).orElseThrow().getCategoryId()).isEqualTo(101L);
        var schedule = schedules.findById(301L).orElseThrow();
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getCreatedAt()).isEqualTo(SOURCE_TIME);
        var state = states.findById(1).orElseThrow();
        assertThat(state.getAppliedVersion()).isZero();
        assertThat(state.getAppliedAt()).isAfter(SOURCE_TIME);
    }

    @Test
    void replacesExistingIdsAndRemovesRecordsAbsentFromNewSnapshot() {
        store.apply(snapshot(1, "Anterior"));
        var replacement = new CatalogSnapshot(2, SOURCE_TIME,
            List.of(category("Actualizada")), List.of(), List.of());
        assertThat(store.apply(replacement)).isTrue();
        assertThat(categories.findAll()).singleElement()
            .satisfies(category -> assertThat(category.getName()).isEqualTo("Actualizada"));
        assertThat(professionals.count()).isZero();
        assertThat(schedules.count()).isZero();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(2);
    }

    @Test
    void ignoresRepeatedAndOlderVersionsWithoutChangingDataOrApplicationTime() {
        store.apply(snapshot(7, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        assertThat(store.apply(snapshot(7, "Duplicada"))).isFalse();
        assertThat(store.apply(snapshot(6, "Antigua"))).isFalse();
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(7);
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
    }

    @Test
    void emptySnapshotClearsAllCollectionsAndAdvancesVersion() {
        store.apply(snapshot(1, "Original"));
        assertThat(store.apply(new CatalogSnapshot(2, SOURCE_TIME, List.of(), List.of(), List.of()))).isTrue();
        assertThat(categories.count()).isZero();
        assertThat(professionals.count()).isZero();
        assertThat(schedules.count()).isZero();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(2);
    }

    @Test
    void databaseFailureRestoresAllPreviousDataAndVersion() {
        store.apply(snapshot(1, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        var invalidProfessional = new Professional(201L, 101L, "x".repeat(70000), "Gómez",
            true, SOURCE_TIME, SOURCE_TIME);
        var invalid = new CatalogSnapshot(2, SOURCE_TIME, List.of(category("Nueva")),
            List.of(invalidProfessional), List.of());
        // Supera el límite TEXT de MySQL después de borrar la copia anterior.
        var useCase = new SynchronizeCatalogSnapshotService(() -> invalid, store);
        assertThatThrownBy(useCase::synchronize).isInstanceOf(DataAccessException.class);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(professionals.findById(201L).orElseThrow().getFirstName()).isEqualTo("Ana");
        assertThat(schedules.findById(301L)).isPresent();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(1);
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
    }

    @Test
    void missingStateAndNullSnapshotFailBeforeReplacingData() {
        store.apply(snapshot(1, "Original"));
        assertThatNullPointerException().isThrownBy(() -> store.apply(null));
        jdbc.update("DELETE FROM catalog_sync_state");
        assertThatThrownBy(() -> store.apply(snapshot(2, "Nueva")))
            .isInstanceOf(DataAccessException.class).hasRootCauseInstanceOf(IllegalStateException.class);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
    }

    @Test
    void synchronizationFetchesOutsideTransactionThenPersistsAndIgnoresDuplicate() {
        var builder = RestClient.builder().baseUrl("http://catedra.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        for (int i = 0; i < 2; i++) {
            server.expect(requestTo("http://catedra.test/api/synchronization/snapshot"))
                .andExpect(request -> assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse())
                .andRespond(withSuccess(new ClassPathResource("catalog/snapshot.json"), MediaType.APPLICATION_JSON));
        }
        var useCase = new SynchronizeCatalogSnapshotService(new RestCatalogSnapshotSource(builder.build()), store);
        assertThat(useCase.synchronize()).isTrue();
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(7);
        assertThat(categories.findById(1L)).isPresent();
        assertThat(professionals.findById(15L)).isPresent();
        assertThat(schedules.findById(21L)).isPresent();
        assertThat(useCase.synchronize()).isFalse();
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
        server.verify();
    }

    @Test
    void remoteFailureKeepsPreviouslyAppliedCatalog() {
        store.apply(snapshot(1, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        var builder = RestClient.builder().baseUrl("http://catedra.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://catedra.test/api/synchronization/snapshot"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        var useCase = new SynchronizeCatalogSnapshotService(new RestCatalogSnapshotSource(builder.build()), store);
        assertThatThrownBy(useCase::synchronize).isInstanceOf(SnapshotFetchException.class);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(professionals.findById(201L)).isPresent();
        assertThat(schedules.findById(301L)).isPresent();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(1);
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
        server.verify();
    }

    @Test
    void incrementUpsertsParentsAndChildrenAndPreservesUnchangedRows() {
        store.apply(snapshot(1, "Original"));
        var changedCategory = new ProfessionalCategory(102L, "Nueva", null, true, SOURCE_TIME, SOURCE_TIME);
        var changedProfessional = new Professional(202L, 102L, "Eva", "Pérez", true, SOURCE_TIME, SOURCE_TIME);
        var changedSchedule = new WeeklySchedule(302L, 202L, DayOfWeek.TUESDAY,
            LocalTime.of(19, 0), LocalTime.of(22, 0), 45, true, SOURCE_TIME, SOURCE_TIME);
        assertThat(increments.apply(new CatalogChangedEntities(2,
            List.of(category("Actualizada"), changedCategory), List.of(changedProfessional), List.of(changedSchedule)))).isTrue();
        assertThat(categories.count()).isEqualTo(2);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Actualizada");
        assertThat(professionals.count()).isEqualTo(2);
        assertThat(professionals.findById(201L).orElseThrow().getFirstName()).isEqualTo("Ana");
        assertThat(schedules.count()).isEqualTo(2);
        assertThat(schedules.findById(302L).orElseThrow().getStartTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(schedules.findById(302L).orElseThrow().getCreatedAt()).isEqualTo(SOURCE_TIME);
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(2);
    }

    @Test
    void incrementUpdatesChildrenAndKeepsLogicalDeletions() {
        store.apply(snapshot(1, "Original"));
        var professional = new Professional(201L, 101L, "Ana María", "Gómez", false, SOURCE_TIME, SOURCE_TIME);
        var schedule = new WeeklySchedule(301L, 201L, DayOfWeek.FRIDAY,
            LocalTime.of(10, 0), LocalTime.of(11, 0), 15, false, SOURCE_TIME, SOURCE_TIME);
        assertThat(increments.apply(new CatalogChangedEntities(2, List.of(), List.of(professional), List.of(schedule)))).isTrue();
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(professionals.findById(201L).orElseThrow().getFirstName()).isEqualTo("Ana María");
        assertThat(professionals.findById(201L).orElseThrow().getEnabled()).isFalse();
        assertThat(schedules.findById(301L).orElseThrow().getEnabled()).isFalse();
        assertThat(schedules.findById(301L).orElseThrow().getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
    }

    @Test
    void emptyIncrementAdvancesVersionWithoutDeletingRows() {
        store.apply(snapshot(0, "Original"));
        assertThat(increments.apply(new CatalogChangedEntities(1, List.of(), List.of(), List.of()))).isTrue();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(1);
        assertThat(categories.count()).isEqualTo(1);
        assertThat(professionals.count()).isEqualTo(1);
        assertThat(schedules.count()).isEqualTo(1);
    }

    @Test
    void repeatedOrOlderIncrementDoesNotChangeDataOrTimestamp() {
        store.apply(snapshot(7, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        assertThat(increments.apply(new CatalogChangedEntities(7, List.of(category("Otra")), List.of(), List.of()))).isFalse();
        assertThat(increments.apply(new CatalogChangedEntities(6, List.of(category("Antigua")), List.of(), List.of()))).isFalse();
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
    }

    @Test
    void incrementRequiresInitializedStateAndConsecutiveVersion() {
        var changes = new CatalogChangedEntities(2, List.of(category("Nueva")), List.of(), List.of());
        assertThatThrownBy(() -> increments.apply(changes)).isInstanceOf(CatalogIncrementRejectedException.class);
        assertThat(categories.count()).isZero();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isNull();
        store.apply(snapshot(0, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        assertThatThrownBy(() -> increments.apply(changes)).isInstanceOf(CatalogIncrementRejectedException.class);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isZero();
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
    }

    @Test
    void missingStateRejectsIncrement() {
        jdbc.update("DELETE FROM catalog_sync_state");
        assertThatThrownBy(() -> increments.apply(
            new CatalogChangedEntities(1, List.of(category("Nueva")), List.of(), List.of())))
            .isInstanceOf(CatalogIncrementRejectedException.class);
        assertThat(categories.count()).isZero();
    }

    @Test
    void invalidChildRollsBackParentUpdatesInsertsAndVersion() {
        store.apply(snapshot(1, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        var professional = new Professional(202L, 101L, "Eva", "Pérez", true, SOURCE_TIME, SOURCE_TIME);
        var invalidSchedule = new WeeklySchedule(302L, 999L, DayOfWeek.MONDAY,
            LocalTime.of(9, 0), LocalTime.of(10, 0), 30, true, SOURCE_TIME, SOURCE_TIME);
        assertThatThrownBy(() -> increments.apply(new CatalogChangedEntities(2,
            List.of(category("No debe persistir")), List.of(professional), List.of(invalidSchedule))))
            .isInstanceOf(DataAccessException.class);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(professionals.findById(202L)).isEmpty();
        assertThat(schedules.findById(302L)).isEmpty();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(1);
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
    }

    @Test
    void concurrentDuplicateIncrementIsAppliedOnlyOnce() throws Exception {
        store.apply(snapshot(1, "Original"));
        var changes = new CatalogChangedEntities(2, List.of(category("Actualizada")), List.of(), List.of());
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> task = () -> {
                start.await();
                return increments.apply(changes);
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();
            assertThat(List.of(first.get(15, java.util.concurrent.TimeUnit.SECONDS),
                second.get(15, java.util.concurrent.TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(2);
    }

    @Test
    void coordinatorReadsOutsideTransactionAndAppliesThroughRealStore() {
        store.apply(snapshot(1, "Original"));
        var reads = new java.util.concurrent.atomic.AtomicInteger();
        var changedIds = new ar.edu.programacion2.catalog.catalog.application.model.CatalogChanges(
            2, List.of(101L), List.of(), List.of());
        var batch = new CatalogChangedEntities(2, List.of(category("Desde Redis")), List.of(), List.of());
        var coordinator = new ar.edu.programacion2.catalog.catalog.application.service.SynchronizeCatalogIncrementService(
            () -> {
                var state = states.findById(1).orElseThrow();
                return new ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationStatus(
                    state.getAppliedVersion(), state.getAppliedAt());
            },
            () -> {
                assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
                reads.incrementAndGet();
                return new ar.edu.programacion2.catalog.catalog.application.model.RemoteCatalogVersions(2, 1);
            },
            version -> {
                assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
                assertThat(version).isEqualTo(2);
                return java.util.Optional.of(changedIds);
            },
            ids -> {
                assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
                assertThat(ids).isEqualTo(changedIds);
                return batch;
            }, increments);
        assertThat(coordinator.synchronize()).isEqualTo(
            ar.edu.programacion2.catalog.catalog.application.model.CatalogIncrementResult.APPLIED);
        assertThat(reads.get()).isEqualTo(2);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Desde Redis");
        assertThat(professionals.count()).isEqualTo(1);
        assertThat(schedules.count()).isEqualTo(1);
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(2);
        assertThat(coordinator.synchronize()).isEqualTo(
            ar.edu.programacion2.catalog.catalog.application.model.CatalogIncrementResult.UP_TO_DATE);
    }

    @Test
    void recoveryDownloadsAndPersistsSnapshotWhenSeveralVersionsArePending() {
        store.apply(snapshot(1, "Anterior"));
        var changes = mock(CatalogChangesSource.class);
        var entities = mock(CatalogEntitiesSource.class);
        var builder = RestClient.builder().baseUrl("http://catedra.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://catedra.test/api/synchronization/snapshot"))
            .andExpect(request -> assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse())
            .andRespond(withSuccess(new ClassPathResource("catalog/snapshot.json"), MediaType.APPLICATION_JSON));
        var recovery = new SynchronizeCatalogService(recoveryIncrement(changes, entities),
            new SynchronizeCatalogSnapshotService(new RestCatalogSnapshotSource(builder.build()), store));
        assertThat(recovery.synchronize()).isEqualTo(CatalogSynchronizationResult.SNAPSHOT_APPLIED);
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(7);
        assertThat(categories.findById(101L)).isEmpty();
        assertThat(categories.findById(1L)).isPresent();
        assertThat(professionals.findById(15L)).isPresent();
        assertThat(schedules.findById(21L)).isPresent();
        assertThat(recovery.synchronize()).isEqualTo(CatalogSynchronizationResult.UP_TO_DATE);
        verifyNoInteractions(changes, entities);
        server.verify();
    }

    @Test
    void failedRecoveryPreservesLocalCatalogAndVersion() {
        store.apply(snapshot(1, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        var changes = mock(CatalogChangesSource.class);
        var entities = mock(CatalogEntitiesSource.class);
        var builder = RestClient.builder().baseUrl("http://catedra.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://catedra.test/api/synchronization/snapshot"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        var recovery = new SynchronizeCatalogService(recoveryIncrement(changes, entities),
            new SynchronizeCatalogSnapshotService(new RestCatalogSnapshotSource(builder.build()), store));
        assertThatThrownBy(recovery::synchronize).isInstanceOf(SnapshotFetchException.class);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(professionals.findById(201L)).isPresent();
        assertThat(schedules.findById(301L)).isPresent();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(1);
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
        verifyNoInteractions(changes, entities);
        server.verify();
    }

    private SynchronizeCatalogIncrement recoveryIncrement(CatalogChangesSource changes, CatalogEntitiesSource entities) {
        return new SynchronizeCatalogIncrementService(() -> {
            var state = states.findById(1).orElseThrow();
            return new CatalogSynchronizationStatus(state.getAppliedVersion(), state.getAppliedAt());
        }, () -> new RemoteCatalogVersions(7, 1), changes, entities, increments);
    }

    @Test
    void processedEventIsDurableIdempotentAndCaseSensitive() {
        store.apply(snapshot(7, "Original"));
        var event = catalogEvent("Event-7", 7);
        assertThat(processedEvents.isProcessed(event)).isFalse();
        processedEvents.markProcessed(event);
        assertThat(processedEvents.isProcessed(event)).isTrue();
        var processedAt = jdbc.queryForObject("SELECT processed_at FROM processed_catalog_event", java.sql.Timestamp.class);
        processedEvents.markProcessed(event);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM processed_catalog_event", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT processed_at FROM processed_catalog_event", java.sql.Timestamp.class)).isEqualTo(processedAt);
        assertThat(processedEvents.isProcessed(catalogEvent("event-7", 7))).isFalse();
        processedEvents.markProcessed(catalogEvent("event-7", 7));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM processed_catalog_event", Integer.class)).isEqualTo(2);
    }

    @Test
    void eventCannotBeRecordedBeforeLocalVersionCoversIt() {
        var event = catalogEvent("event-7", 7);
        assertThatThrownBy(() -> processedEvents.markProcessed(event)).isInstanceOf(DataAccessException.class);
        store.apply(snapshot(6, "Original"));
        assertThatThrownBy(() -> processedEvents.markProcessed(event)).isInstanceOf(DataAccessException.class);
        assertThat(processedEvents.isProcessed(event)).isFalse();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(6);
    }

    @Test
    void reusedEventIdWithDifferentVersionIsRejected() {
        store.apply(snapshot(8, "Original"));
        processedEvents.markProcessed(catalogEvent("event-7", 7));
        var conflict = catalogEvent("event-7", 8);
        assertThatThrownBy(() -> processedEvents.isProcessed(conflict))
            .isInstanceOf(ar.edu.programacion2.catalog.catalog.application.port.out.CatalogEventConflictException.class);
        assertThatThrownBy(() -> processedEvents.markProcessed(conflict))
            .isInstanceOf(ar.edu.programacion2.catalog.catalog.application.port.out.CatalogEventConflictException.class);
        assertThat(jdbc.queryForObject("SELECT new_version FROM processed_catalog_event", Long.class)).isEqualTo(7);
    }

    @Test
    void concurrentEventRecordingCreatesOneRow() throws Exception {
        store.apply(snapshot(7, "Original"));
        var event = catalogEvent("event-7", 7);
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Void> task = () -> {
                start.await();
                processedEvents.markProcessed(event);
                return null;
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();
            first.get(15, java.util.concurrent.TimeUnit.SECONDS);
            second.get(15, java.util.concurrent.TimeUnit.SECONDS);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM processed_catalog_event", Integer.class)).isEqualTo(1);
    }

    @Test
    void eventHandlerSynchronizesOutsideTransactionThenPersistsDeduplication() {
        var calls = new java.util.concurrent.atomic.AtomicInteger();
        var handler = new ar.edu.programacion2.catalog.catalog.application.service.HandleCatalogUpdatedService(
            processedEvents, () -> {
                var state = states.findById(1).orElseThrow();
                return new CatalogSynchronizationStatus(state.getAppliedVersion(), state.getAppliedAt());
            }, () -> {
                assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
                calls.incrementAndGet();
                store.apply(snapshot(7, "Notificada"));
                return CatalogSynchronizationResult.SNAPSHOT_APPLIED;
            });
        var event = catalogEvent("event-7", 7);
        assertThat(handler.handle(event)).isEqualTo(ar.edu.programacion2.catalog.catalog.application.model.CatalogEventResult.PROCESSED);
        assertThat(handler.handle(event)).isEqualTo(ar.edu.programacion2.catalog.catalog.application.model.CatalogEventResult.DUPLICATE);
        assertThat(calls.get()).isEqualTo(1);
        assertThat(processedEvents.isProcessed(event)).isTrue();
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Notificada");
    }

    private ar.edu.programacion2.catalog.catalog.application.model.CatalogUpdatedEvent catalogEvent(String id, long version) {
        return new ar.edu.programacion2.catalog.catalog.application.model.CatalogUpdatedEvent(id, version, SOURCE_TIME, "MANUAL");
    }

    private static ProfessionalCategory category(String name) {
        return new ProfessionalCategory(101L, name, null, false, SOURCE_TIME, SOURCE_TIME);
    }

    private static CatalogSnapshot snapshot(long version, String name) {
        return new CatalogSnapshot(version, SOURCE_TIME, List.of(category(name)),
            List.of(new Professional(201L, 101L, "Ana", "Gómez", false, SOURCE_TIME, SOURCE_TIME)),
            List.of(new WeeklySchedule(301L, 201L, DayOfWeek.MONDAY, LocalTime.of(9, 0),
                LocalTime.of(12, 0), 30, false, SOURCE_TIME, SOURCE_TIME)));
    }
}
