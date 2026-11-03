package ar.edu.programacion2.catalog.catalog.application.model;

public enum CatalogSynchronizationResult {
    INCREMENT_APPLIED,
    ALREADY_APPLIED,
    UP_TO_DATE,
    SNAPSHOT_APPLIED,
    SNAPSHOT_ALREADY_COVERED,
    RETRY_REQUIRED,
    LOCAL_VERSION_AHEAD
}
