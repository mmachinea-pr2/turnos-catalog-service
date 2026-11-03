package ar.edu.programacion2.catalog.catalog.application.model;

public enum CatalogEventResult {
    PROCESSED,
    DUPLICATE,
    RETRY_REQUIRED,
    LOCAL_VERSION_AHEAD
}
