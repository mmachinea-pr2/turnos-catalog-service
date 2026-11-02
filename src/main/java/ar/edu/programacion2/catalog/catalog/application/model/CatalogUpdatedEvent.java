package ar.edu.programacion2.catalog.catalog.application.model;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

public record CatalogUpdatedEvent(String eventId, long newVersion, Instant occurredAt, String source) {
    public CatalogUpdatedEvent {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(occurredAt);
        Objects.requireNonNull(source);
        if (eventId.isBlank() || eventId.getBytes(StandardCharsets.UTF_8).length > 255
                || newVersion < 0 || source.isBlank()) {
            throw new IllegalArgumentException("Invalid catalog event");
        }
    }
}
