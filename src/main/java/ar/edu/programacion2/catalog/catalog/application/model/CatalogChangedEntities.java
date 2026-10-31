package ar.edu.programacion2.catalog.catalog.application.model;

import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.util.List;

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
    }
}
