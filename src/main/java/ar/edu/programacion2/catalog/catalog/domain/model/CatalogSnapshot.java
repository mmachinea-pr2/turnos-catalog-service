package ar.edu.programacion2.catalog.catalog.domain.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/** Catálogo completo validado; no contiene estado ni dependencias de persistencia. */
public record CatalogSnapshot(
    long snapshotVersion,
    Instant generatedAt,
    List<ProfessionalCategory> professionalCategories,
    List<Professional> professionals,
    List<WeeklySchedule> weeklySchedules
) {
    public CatalogSnapshot {
        if (snapshotVersion < 0) {
            throw new IllegalArgumentException("Snapshot version must not be negative");
        }
        Objects.requireNonNull(generatedAt, "generatedAt is required");
        professionalCategories = List.copyOf(professionalCategories);
        professionals = List.copyOf(professionals);
        weeklySchedules = List.copyOf(weeklySchedules);

        Set<Long> categoryIds = uniqueIds(professionalCategories, ProfessionalCategory::id, "category");
        Set<Long> professionalIds = uniqueIds(professionals, Professional::id, "professional");
        uniqueIds(weeklySchedules, WeeklySchedule::id, "schedule");

        for (Professional professional : professionals) {
            if (!categoryIds.contains(professional.categoryId())) {
                throw new IllegalArgumentException("Professional references a missing category");
            }
        }
        for (WeeklySchedule schedule : weeklySchedules) {
            if (!professionalIds.contains(schedule.professionalId())) {
                throw new IllegalArgumentException("Schedule references a missing professional");
            }
        }
    }

    private static <T> Set<Long> uniqueIds(List<T> items, Function<T, Long> idOf, String entity) {
        Set<Long> ids = new HashSet<>();
        for (T item : items) {
            if (!ids.add(idOf.apply(item))) {
                throw new IllegalArgumentException("Duplicate " + entity + " ID in snapshot");
            }
        }
        return ids;
    }
}
