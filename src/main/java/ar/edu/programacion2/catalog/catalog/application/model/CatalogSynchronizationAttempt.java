package ar.edu.programacion2.catalog.catalog.application.model;

import java.time.Instant;

public record CatalogSynchronizationAttempt(Instant startedAt, Instant completedAt,
        String result, String errorCode) {}
