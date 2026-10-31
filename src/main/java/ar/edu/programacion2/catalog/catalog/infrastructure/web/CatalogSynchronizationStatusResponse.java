package ar.edu.programacion2.catalog.catalog.infrastructure.web;

import java.time.Instant;

public record CatalogSynchronizationStatusResponse(boolean initialized, Long appliedVersion, Instant appliedAt) {}
