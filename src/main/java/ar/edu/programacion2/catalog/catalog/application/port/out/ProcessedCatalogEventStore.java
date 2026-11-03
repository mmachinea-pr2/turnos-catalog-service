package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogUpdatedEvent;

public interface ProcessedCatalogEventStore {
    boolean isProcessed(CatalogUpdatedEvent event);
    /** Requires the local version to cover the event; must be idempotent. */
    void markProcessed(CatalogUpdatedEvent event);
}
