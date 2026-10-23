package ar.edu.programacion2.catalog.catalog.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Professional(Long id, Long categoryId, String firstName, String lastName, boolean enabled, Instant createdAt, Instant updatedAt) {
    public Professional {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(categoryId, "categoryId is required");
        Objects.requireNonNull(firstName, "firstName is required");
        Objects.requireNonNull(lastName, "lastName is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
    }
}
