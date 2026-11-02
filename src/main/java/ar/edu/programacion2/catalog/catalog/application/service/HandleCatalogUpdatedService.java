package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.*;
import ar.edu.programacion2.catalog.catalog.application.port.in.HandleCatalogUpdated;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationStatusReader;
import ar.edu.programacion2.catalog.catalog.application.port.out.ProcessedCatalogEventStore;
import java.util.Objects;

public class HandleCatalogUpdatedService implements HandleCatalogUpdated {
    private final ProcessedCatalogEventStore events;
    private final CatalogSynchronizationStatusReader status;
    private final SynchronizeCatalog synchronize;

    public HandleCatalogUpdatedService(ProcessedCatalogEventStore events,
            CatalogSynchronizationStatusReader status, SynchronizeCatalog synchronize) {
        this.events = events;
        this.status = status;
        this.synchronize = synchronize;
    }

    @Override
    public CatalogEventResult handle(CatalogUpdatedEvent event) {
        Objects.requireNonNull(event);
        if (events.isProcessed(event)) {
            return CatalogEventResult.DUPLICATE;
        }
        if (!covers(event)) {
            var result = Objects.requireNonNull(synchronize.synchronize());
            if (result == CatalogSynchronizationResult.RETRY_REQUIRED) {
                return CatalogEventResult.RETRY_REQUIRED;
            }
            if (result == CatalogSynchronizationResult.LOCAL_VERSION_AHEAD) {
                return CatalogEventResult.LOCAL_VERSION_AHEAD;
            }
            if (!covers(event)) {
                return CatalogEventResult.RETRY_REQUIRED;
            }
        }
        events.markProcessed(event);
        return CatalogEventResult.PROCESSED;
    }

    private boolean covers(CatalogUpdatedEvent event) {
        var version = status.read().appliedVersion();
        return version != null && version >= event.newVersion();
    }
}
