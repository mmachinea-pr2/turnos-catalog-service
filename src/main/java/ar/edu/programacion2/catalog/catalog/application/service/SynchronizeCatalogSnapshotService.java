package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotStore;
import java.util.Objects;

/** Coordina la consulta externa y el guardado sin abrir una transacción durante la red. */
public class SynchronizeCatalogSnapshotService implements SynchronizeCatalogSnapshot {
    private final CatalogSnapshotSource source;
    private final CatalogSnapshotStore store;

    public SynchronizeCatalogSnapshotService(CatalogSnapshotSource source, CatalogSnapshotStore store) {
        this.source = Objects.requireNonNull(source);
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public boolean synchronize() {
        var snapshot = Objects.requireNonNull(source.fetch(), "Snapshot source returned no snapshot");
        return store.apply(snapshot);
    }
}
