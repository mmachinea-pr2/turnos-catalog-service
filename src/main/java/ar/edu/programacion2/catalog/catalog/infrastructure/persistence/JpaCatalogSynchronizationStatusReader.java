package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationStatus;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationStatusReader;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCatalogSynchronizationStatusReader implements CatalogSynchronizationStatusReader {
    private final CatalogSyncStateRepository states;

    public JpaCatalogSynchronizationStatusReader(CatalogSyncStateRepository states) {
        this.states = states;
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogSynchronizationStatus read() {
        var state = states.findById(1)
            .orElseThrow(() -> new IllegalStateException("Catalog synchronization state is missing"));
        return new CatalogSynchronizationStatus(state.getAppliedVersion(), state.getAppliedAt());
    }
}
