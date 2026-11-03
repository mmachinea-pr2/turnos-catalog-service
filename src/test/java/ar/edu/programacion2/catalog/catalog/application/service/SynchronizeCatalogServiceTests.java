package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogIncrementResult;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogIncrement;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionReadException;
import ar.edu.programacion2.catalog.catalog.application.port.out.SnapshotFetchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SynchronizeCatalogServiceTests {
    private final SynchronizeCatalogIncrement increment = mock(SynchronizeCatalogIncrement.class);
    private final SynchronizeCatalogSnapshot snapshot = mock(SynchronizeCatalogSnapshot.class);
    private final SynchronizeCatalogService service = new SynchronizeCatalogService(increment, snapshot);

    @ParameterizedTest
    @CsvSource({"APPLIED,INCREMENT_APPLIED", "ALREADY_APPLIED,ALREADY_APPLIED", "UP_TO_DATE,UP_TO_DATE",
        "RETRY_REQUIRED,RETRY_REQUIRED", "LOCAL_VERSION_AHEAD,LOCAL_VERSION_AHEAD"})
    void doesNotDownloadSnapshotForOtherOutcomes(CatalogIncrementResult result, CatalogSynchronizationResult expected) {
        when(increment.synchronize()).thenReturn(result);
        assertThat(service.synchronize()).isEqualTo(expected);
        verify(increment).synchronize();
        verifyNoInteractions(snapshot);
        verifyNoMoreInteractions(increment);
    }

    @ParameterizedTest
    @CsvSource({"true,SNAPSHOT_APPLIED", "false,SNAPSHOT_ALREADY_COVERED"})
    void recoversOnceWhenSnapshotIsRequired(boolean applied, CatalogSynchronizationResult expected) {
        when(increment.synchronize()).thenReturn(CatalogIncrementResult.SNAPSHOT_REQUIRED);
        when(snapshot.synchronize()).thenReturn(applied);
        assertThat(service.synchronize()).isEqualTo(expected);
        var order = inOrder(increment, snapshot);
        order.verify(increment).synchronize();
        order.verify(snapshot).synchronize();
        order.verifyNoMoreInteractions();
    }

    @Test
    void redisFailureIsNotSilentlyTreatedAsSnapshotRequired() {
        var failure = new CatalogVersionReadException();
        when(increment.synchronize()).thenThrow(failure);
        assertThatThrownBy(service::synchronize).isSameAs(failure);
        verifyNoInteractions(snapshot);
    }

    @Test
    void snapshotFailureIsPropagatedWithoutRetrying() {
        when(increment.synchronize()).thenReturn(CatalogIncrementResult.SNAPSHOT_REQUIRED);
        var failure = new SnapshotFetchException("Snapshot server unavailable");
        when(snapshot.synchronize()).thenThrow(failure);
        assertThatThrownBy(service::synchronize).isSameAs(failure);
        verify(snapshot).synchronize();
        verifyNoMoreInteractions(snapshot);
    }
}
