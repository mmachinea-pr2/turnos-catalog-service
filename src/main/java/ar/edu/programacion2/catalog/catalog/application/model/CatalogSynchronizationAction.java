package ar.edu.programacion2.catalog.catalog.application.model;

public enum CatalogSynchronizationAction {
    UP_TO_DATE,
    FETCH_SNAPSHOT,
    APPLY_INCREMENTS,
    LOCAL_VERSION_AHEAD
}
