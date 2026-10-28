package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotStore;
import ar.edu.programacion2.catalog.catalog.application.port.out.SnapshotFetchException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SynchronizeCatalogSnapshotServiceTests {
    @Test
    void failedFetchNeverCallsPersistence() {
        var source = mock(CatalogSnapshotSource.class);
        var store = mock(CatalogSnapshotStore.class);
        when(source.fetch()).thenThrow(new SnapshotFetchException("Server unavailable"));
        var useCase = new SynchronizeCatalogSnapshotService(source, store);
        assertThatThrownBy(useCase::synchronize).isInstanceOf(SnapshotFetchException.class);
        verifyNoInteractions(store);
    }

    @Test
    void missingSnapshotNeverCallsPersistence() {
        var store = mock(CatalogSnapshotStore.class);
        var useCase = new SynchronizeCatalogSnapshotService(() -> null, store);
        assertThatNullPointerException().isThrownBy(useCase::synchronize);
        verifyNoInteractions(store);
    }
}
