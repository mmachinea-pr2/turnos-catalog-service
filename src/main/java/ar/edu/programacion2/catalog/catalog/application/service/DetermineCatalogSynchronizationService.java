package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationAction;
import ar.edu.programacion2.catalog.catalog.application.port.in.DetermineCatalogSynchronization;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationStatusReader;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionSource;

public class DetermineCatalogSynchronizationService implements DetermineCatalogSynchronization {
    private final CatalogSynchronizationStatusReader statusReader;
    private final CatalogVersionSource versionSource;

    public DetermineCatalogSynchronizationService(CatalogSynchronizationStatusReader statusReader,
            CatalogVersionSource versionSource) {
        this.statusReader = statusReader;
        this.versionSource = versionSource;
    }

    @Override
    public CatalogSynchronizationAction determine() {
        var localVersion = statusReader.read().appliedVersion();
        if (localVersion != null && localVersion < 0) {
            throw new IllegalStateException("Local catalog version must not be negative");
        }
        var remote = versionSource.read();
        if (localVersion == null) {
            return CatalogSynchronizationAction.FETCH_SNAPSHOT;
        }
        if (localVersion > remote.currentVersion()) {
            return CatalogSynchronizationAction.LOCAL_VERSION_AHEAD;
        }
        if (localVersion == remote.currentVersion()) {
            return CatalogSynchronizationAction.UP_TO_DATE;
        }
        if (localVersion < remote.oldestAvailableVersion()) {
            return CatalogSynchronizationAction.FETCH_SNAPSHOT;
        }
        return CatalogSynchronizationAction.APPLY_INCREMENTS;
    }
}
