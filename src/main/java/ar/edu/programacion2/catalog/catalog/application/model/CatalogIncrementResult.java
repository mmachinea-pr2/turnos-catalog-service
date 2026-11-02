package ar.edu.programacion2.catalog.catalog.application.model;

public enum CatalogIncrementResult {
    APPLIED,
    ALREADY_APPLIED,
    UP_TO_DATE,
    SNAPSHOT_REQUIRED,
    RETRY_REQUIRED,
    LOCAL_VERSION_AHEAD
}
