package ar.edu.programacion2.catalog.catalog.application.model;

import java.util.HashSet;
import java.util.List;

public record CatalogChanges(long version, List<Long> professionalCategories,
        List<Long> professionals, List<Long> weeklySchedules) {
    public CatalogChanges {
        if (version < 0) {
            throw new IllegalArgumentException("Version must not be negative");
        }
        professionalCategories = validatedCopy(professionalCategories);
        professionals = validatedCopy(professionals);
        weeklySchedules = validatedCopy(weeklySchedules);
    }

    private static List<Long> validatedCopy(List<Long> ids) {
        var copy = List.copyOf(ids);
        if (copy.stream().anyMatch(id -> id <= 0) || new HashSet<>(copy).size() != copy.size()) {
            throw new IllegalArgumentException("Changed IDs must be positive and unique within each collection");
        }
        return copy;
    }
}
