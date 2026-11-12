package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationAttemptStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackCatalogSynchronizationTests {
    private final CatalogSynchronizationAttemptStore store = mock(CatalogSynchronizationAttemptStore.class);
    private final TrackCatalogSynchronization tracker = new TrackCatalogSynchronization(store,
        Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

    @ParameterizedTest
    @EnumSource(CatalogSynchronizationResult.class)
    void recordsEveryResultIncludingRecoveryWarnings(CatalogSynchronizationResult result) {
        assertThat(tracker.execute(() -> result, Enum::name)).isEqualTo(result);
        var order = inOrder(store);
        order.verify(store).start(any(UUID.class), eq(Instant.EPOCH));
        order.verify(store).complete(any(UUID.class), eq(Instant.EPOCH), eq(result.name()), isNull());
        order.verifyNoMoreInteractions();
    }

    @Test
    void nestedSnapshotBelongsToOuterAttempt() {
        assertThat(tracker.execute(() -> tracker.execute(() -> true, value -> "INNER"), value -> "OUTER")).isTrue();
        verify(store, times(1)).start(any(), any());
        verify(store).complete(any(), any(), eq("OUTER"), isNull());
        verifyNoMoreInteractions(store);
    }

    @Test
    void failureRecordsOnlySafeCodeAndNextAttemptCanRecover() {
        var failure = new IllegalArgumentException("private remote response");
        assertThatThrownBy(() -> tracker.execute(() -> { throw failure; }, Object::toString)).isSameAs(failure);
        verify(store).complete(any(), any(), eq("FAILED"), eq("SYNCHRONIZATION_FAILED"));
        assertThat(tracker.execute(() -> "UP_TO_DATE", value -> value)).isEqualTo("UP_TO_DATE");
        verify(store, times(2)).start(any(), any());
        verify(store).complete(any(), any(), eq("UP_TO_DATE"), isNull());
    }

    @Test
    void recordingFailureDoesNotHideOriginalFailureOrLeakThreadState() {
        var original = new IllegalStateException("original");
        var recording = new IllegalStateException("database unavailable");
        doThrow(recording).when(store).complete(any(), any(), eq("FAILED"), any());
        assertThatThrownBy(() -> tracker.execute(() -> { throw original; }, Object::toString)).isSameAs(original);
        assertThat(original.getSuppressed()).containsExactly(recording);
        tracker.execute(() -> "OK", value -> value);
        verify(store, times(2)).start(any(), any());
    }

    @Test
    void failureToStartPreventsOperation() {
        doThrow(new IllegalStateException("database unavailable")).when(store).start(any(), any());
        var operation = mock(java.util.function.Supplier.class);
        assertThatThrownBy(() -> tracker.execute(operation, Object::toString)).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(operation);
    }

    @Test
    void failedSuccessRecordingIsNotReportedAsSynchronizationFailure() {
        doThrow(new IllegalStateException("database unavailable")).when(store).complete(any(), any(), eq("OK"), isNull());
        assertThatThrownBy(() -> tracker.execute(() -> "OK", value -> value)).isInstanceOf(IllegalStateException.class);
        verify(store, never()).complete(any(), any(), eq("FAILED"), any());
        tracker.execute(() -> "RECOVERED", value -> value);
        verify(store, times(2)).start(any(), any());
    }
}
