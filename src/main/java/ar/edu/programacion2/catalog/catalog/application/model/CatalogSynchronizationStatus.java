package ar.edu.programacion2.catalog.catalog.application.model;

import java.time.Instant;

/** Estado de la última aplicación local; no indica disponibilidad de la cátedra. */
public record CatalogSynchronizationStatus(Long appliedVersion, Instant appliedAt) {
    public boolean initialized() {
        return appliedVersion != null;
    }
}
