package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogIncrement;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import java.util.Objects;

/** Coordinates a single attempt, without network calls inside a transaction or automatic retries. */
public class SynchronizeCatalogService implements SynchronizeCatalog {
    private final SynchronizeCatalogIncrement increment;
    private final SynchronizeCatalogSnapshot snapshot;

    public SynchronizeCatalogService(SynchronizeCatalogIncrement increment, SynchronizeCatalogSnapshot snapshot) {
        this.increment = Objects.requireNonNull(increment);
        this.snapshot = Objects.requireNonNull(snapshot);
    }

    @Override
    public CatalogSynchronizationResult synchronize() {
        return switch (increment.synchronize()) {
            case APPLIED -> CatalogSynchronizationResult.INCREMENT_APPLIED;
            case ALREADY_APPLIED -> CatalogSynchronizationResult.ALREADY_APPLIED;
            case UP_TO_DATE -> CatalogSynchronizationResult.UP_TO_DATE;
            case RETRY_REQUIRED -> CatalogSynchronizationResult.RETRY_REQUIRED;
            case LOCAL_VERSION_AHEAD -> CatalogSynchronizationResult.LOCAL_VERSION_AHEAD;
            case SNAPSHOT_REQUIRED -> snapshot.synchronize()
                ? CatalogSynchronizationResult.SNAPSHOT_APPLIED
                : CatalogSynchronizationResult.SNAPSHOT_ALREADY_COVERED;
        };
    }
}
