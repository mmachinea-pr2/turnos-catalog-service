package ar.edu.programacion2.catalog.catalog.domain.model;

import java.time.Instant;
import java.util.Objects;

public record ProfessionalCategory(Long id, String name, String description, boolean enabled, Instant createdAt, Instant updatedAt) {
    public ProfessionalCategory {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
    }
}
