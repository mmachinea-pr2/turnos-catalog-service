package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.*;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import ar.edu.programacion2.catalog.catalog.application.port.out.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandleCatalogUpdatedServiceTests {
    private final ProcessedCatalogEventStore events = mock(ProcessedCatalogEventStore.class);
    private final CatalogSynchronizationStatusReader status = mock(CatalogSynchronizationStatusReader.class);
    private final SynchronizeCatalog synchronize = mock(SynchronizeCatalog.class);
    private final HandleCatalogUpdatedService service = new HandleCatalogUpdatedService(events, status, synchronize);
    private final CatalogUpdatedEvent event = new CatalogUpdatedEvent("event-7", 7, Instant.EPOCH, "MANUAL");

    @Test
    void duplicateDoesNotCallExternalServices() {
        when(events.isProcessed(event)).thenReturn(true);
        assertThat(service.handle(event)).isEqualTo(CatalogEventResult.DUPLICATE);
        verifyNoInteractions(status, synchronize);
        verify(events, never()).markProcessed(any());
    }

    @Test
    void lateEventIsRecordedWithoutSynchronization() {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(8L, Instant.EPOCH));
        assertThat(service.handle(event)).isEqualTo(CatalogEventResult.PROCESSED);
        verify(events).markProcessed(event);
        verifyNoInteractions(synchronize);
    }

    @Test
    void recordsOnlyAfterSynchronizationCoversTheEvent() {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(null, null),
            new CatalogSynchronizationStatus(7L, Instant.EPOCH));
        when(synchronize.synchronize()).thenReturn(CatalogSynchronizationResult.SNAPSHOT_APPLIED);
        assertThat(service.handle(event)).isEqualTo(CatalogEventResult.PROCESSED);
        var order = inOrder(events, status, synchronize);
        order.verify(events).isProcessed(event);
        order.verify(status).read();
        order.verify(synchronize).synchronize();
        order.verify(status).read();
        order.verify(events).markProcessed(event);
    }

    @ParameterizedTest
    @EnumSource(value = CatalogSynchronizationResult.class,
        names = {"INCREMENT_APPLIED", "SNAPSHOT_APPLIED", "UP_TO_DATE", "ALREADY_APPLIED", "SNAPSHOT_ALREADY_COVERED"})
    void successfulOutcomeDoesNotCoverAnEventAheadOfAvailableData(CatalogSynchronizationResult result) {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(6L, Instant.EPOCH));
        when(synchronize.synchronize()).thenReturn(result);
        assertThat(service.handle(event)).isEqualTo(CatalogEventResult.RETRY_REQUIRED);
        verify(events, never()).markProcessed(any());
    }

    @ParameterizedTest
    @EnumSource(value = CatalogSynchronizationResult.class, names = {"RETRY_REQUIRED", "LOCAL_VERSION_AHEAD"})
    void unresolvedOutcomeIsNotRecorded(CatalogSynchronizationResult result) {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(6L, Instant.EPOCH));
        when(synchronize.synchronize()).thenReturn(result);
        assertThat(service.handle(event)).isEqualTo(CatalogEventResult.valueOf(result.name()));
        verify(events, never()).markProcessed(any());
    }

    @Test
    void failureIsNotRecordedOrRetried() {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(6L, Instant.EPOCH));
        var failure = new IllegalStateException("test failure");
        when(synchronize.synchronize()).thenThrow(failure);
        assertThatThrownBy(() -> service.handle(event)).isSameAs(failure);
        verify(events, never()).markProcessed(any());
        verify(synchronize).synchronize();
    }

    @Test
    void recordingFailureIsPropagated() {
        when(status.read()).thenReturn(new CatalogSynchronizationStatus(7L, Instant.EPOCH));
        var failure = new IllegalStateException("database unavailable");
        doThrow(failure).when(events).markProcessed(event);
        assertThatThrownBy(() -> service.handle(event)).isSameAs(failure);
    }
}
