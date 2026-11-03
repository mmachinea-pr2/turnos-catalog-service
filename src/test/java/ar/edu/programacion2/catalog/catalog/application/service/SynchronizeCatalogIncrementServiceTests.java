package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.*;
import ar.edu.programacion2.catalog.catalog.application.port.out.*;
import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SynchronizeCatalogIncrementServiceTests {
    private final CatalogSynchronizationStatusReader status = mock(CatalogSynchronizationStatusReader.class);
    private final CatalogVersionSource versions = mock(CatalogVersionSource.class);
    private final CatalogChangesSource changes = mock(CatalogChangesSource.class);
    private final CatalogEntitiesSource entities = mock(CatalogEntitiesSource.class);
    private final CatalogIncrementStore store = mock(CatalogIncrementStore.class);
    private final SynchronizeCatalogIncrementService service = new SynchronizeCatalogIncrementService(status, versions, changes, entities, store);
    private final CatalogChanges ids = new CatalogChanges(7, List.of(1L), List.of(), List.of());
    private final CatalogChangedEntities batch = new CatalogChangedEntities(7, List.of(category(1)), List.of(), List.of());

    @BeforeEach
    void ready() {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(6L, null));
        when(versions.read()).thenReturn(new RemoteCatalogVersions(7, 4));
        when(changes.read(7)).thenReturn(Optional.of(ids));
        when(entities.read(ids)).thenReturn(batch);
        when(store.apply(batch)).thenReturn(true);
    }

    @Test
    void readsAndRechecksBeforeSaving() {
        assertThat(service.synchronize()).isEqualTo(CatalogIncrementResult.APPLIED);
        var order = inOrder(status, versions, changes, entities, store);
        order.verify(status).read();
        order.verify(versions).read();
        order.verify(changes).read(7);
        order.verify(entities).read(ids);
        order.verify(versions).read();
        order.verify(store).apply(batch);
        order.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,7,4,SNAPSHOT_REQUIRED", "7,7,4,UP_TO_DATE", "8,7,4,LOCAL_VERSION_AHEAD",
        "3,7,4,SNAPSHOT_REQUIRED", "4,7,4,SNAPSHOT_REQUIRED", "6,7,7,SNAPSHOT_REQUIRED",
        "0,0,0,UP_TO_DATE", "9223372036854775807,9223372036854775807,0,UP_TO_DATE"}, nullValues = "null")
    void stopsWithoutReadingEntitiesOrWritingWhenNotEligible(Long local, long current, long oldest, CatalogIncrementResult expected) {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(local, null));
        when(versions.read()).thenReturn(new RemoteCatalogVersions(current, oldest));
        assertThat(service.synchronize()).isEqualTo(expected);
        verifyNoInteractions(changes, entities, store);
    }

    @Test
    void missingChangeSetRequiresSnapshotWithoutWriting() {
        when(changes.read(7)).thenReturn(Optional.empty());
        assertThat(service.synchronize()).isEqualTo(CatalogIncrementResult.SNAPSHOT_REQUIRED);
        verifyNoInteractions(entities, store);
    }

    @Test
    void validEmptyChangesCanBeApplied() {
        var emptyIds = new CatalogChanges(7, List.of(), List.of(), List.of());
        var emptyBatch = new CatalogChangedEntities(7, List.of(), List.of(), List.of());
        when(changes.read(7)).thenReturn(Optional.of(emptyIds));
        when(entities.read(emptyIds)).thenReturn(emptyBatch);
        when(store.apply(emptyBatch)).thenReturn(true);
        assertThat(service.synchronize()).isEqualTo(CatalogIncrementResult.APPLIED);
        verify(store).apply(emptyBatch);
    }

    @ParameterizedTest
    @CsvSource({"8,4", "7,5", "6,4"})
    void changingRemoteMetadataDiscardsBatch(long current, long oldest) {
        when(versions.read()).thenReturn(new RemoteCatalogVersions(7,4), new RemoteCatalogVersions(current,oldest));
        assertThat(service.synchronize()).isEqualTo(CatalogIncrementResult.RETRY_REQUIRED);
        verifyNoInteractions(store);
    }

    @Test
    void alreadyCoveredByConcurrentWriterDoesNotClaimApplication() {
        when(store.apply(batch)).thenReturn(false);
        assertThat(service.synchronize()).isEqualTo(CatalogIncrementResult.ALREADY_APPLIED);
    }

    @Test
    void mismatchingChangeVersionStopsBeforeEntityRead() {
        when(changes.read(7)).thenReturn(Optional.of(new CatalogChanges(8, List.of(), List.of(), List.of())));
        assertThatThrownBy(service::synchronize).isInstanceOf(CatalogChangesReadException.class);
        verifyNoInteractions(entities, store);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void rejectsIncompleteExtraOrWrongVersionEntityBatches(int variant) {
        var result = switch (variant) {
            case 0 -> new CatalogChangedEntities(7, List.of(), List.of(), List.of());
            case 1 -> new CatalogChangedEntities(7, List.of(category(2)), List.of(), List.of());
            case 2 -> new CatalogChangedEntities(7, List.of(category(1), category(2)), List.of(), List.of());
            default -> new CatalogChangedEntities(8, List.of(category(1)), List.of(), List.of());
        };
        when(entities.read(ids)).thenReturn(result);
        assertThatThrownBy(service::synchronize).isInstanceOf(CatalogEntitiesReadException.class);
        verifyNoInteractions(store);
    }

    @ParameterizedTest
    @ValueSource(ints = {0,1,2,3,4})
    void readFailuresNeverReachPersistence(int stage) {
        var failure = new IllegalStateException("Test source failure");
        switch (stage) {
            case 0 -> when(status.read()).thenThrow(failure);
            case 1 -> when(versions.read()).thenThrow(failure);
            case 2 -> when(changes.read(7)).thenThrow(failure);
            case 3 -> when(entities.read(ids)).thenThrow(failure);
            default -> when(versions.read()).thenReturn(new RemoteCatalogVersions(7,4)).thenThrow(failure);
        }
        assertThatThrownBy(service::synchronize).isSameAs(failure);
        verifyNoInteractions(store);
    }

    @Test
    void propagatesPersistenceRejection() {
        var failure = new CatalogIncrementRejectedException("State changed");
        when(store.apply(batch)).thenThrow(failure);
        assertThatThrownBy(service::synchronize).isSameAs(failure);
    }

    @Test
    void negativeLocalVersionStopsBeforeRemoteRead() {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(-1L, null));
        assertThatIllegalStateException().isThrownBy(service::synchronize);
        verifyNoInteractions(versions, changes, entities, store);
    }

    private static ProfessionalCategory category(long id) {
        var time = Instant.parse("2026-09-10T13:40:00Z");
        return new ProfessionalCategory(id, "Clínica", null, false, time, time);
    }
}
