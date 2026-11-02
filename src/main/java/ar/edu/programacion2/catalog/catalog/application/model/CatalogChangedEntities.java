package ar.edu.programacion2.catalog.catalog.application.model;

import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.util.List;
import java.util.HashSet;
import java.util.function.Function;

/** Current entities for the requested change set; not a historical snapshot. */
public record CatalogChangedEntities(long version, List<ProfessionalCategory> professionalCategories,
        List<Professional> professionals, List<WeeklySchedule> weeklySchedules) {
    public CatalogChangedEntities {
        if (version < 0) {
            throw new IllegalArgumentException("Version must not be negative");
        }
        professionalCategories = List.copyOf(professionalCategories);
        professionals = List.copyOf(professionals);
        weeklySchedules = List.copyOf(weeklySchedules);
        validateIds(professionalCategories, ProfessionalCategory::id);
        validateIds(professionals, Professional::id);
        validateIds(weeklySchedules, WeeklySchedule::id);
    }

    private static <T> void validateIds(List<T> entities, Function<T, Long> identifier) {
        var ids = new HashSet<Long>();
        for (var entity : entities) {
            var id = identifier.apply(entity);
            if (id == null || id <= 0 || !ids.add(id)) {
                throw new IllegalArgumentException("Entity IDs must be positive and unique within each collection");
            }
        }
    }
}
