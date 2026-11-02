package ar.edu.programacion2.catalog.catalog.application.model;

import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.time.Instant;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CatalogChangedEntitiesTests {
    private static final Instant TIME = Instant.parse("2026-09-10T13:40:00Z");

    @Test
    void rejectsDuplicateIdsInEveryCollection() {
        var category = category(1L);
        var professional = new Professional(1L, 1L, "Ana", "Gómez", true, TIME, TIME);
        var schedule = new WeeklySchedule(1L, 1L, DayOfWeek.MONDAY,
            LocalTime.of(9, 0), LocalTime.of(10, 0), 30, true, TIME, TIME);
        assertThatIllegalArgumentException().isThrownBy(() -> new CatalogChangedEntities(1,
            List.of(category, category), List.of(), List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new CatalogChangedEntities(1,
            List.of(), List.of(professional, professional), List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new CatalogChangedEntities(1,
            List.of(), List.of(), List.of(schedule, schedule)));
        assertThat(new CatalogChangedEntities(1, List.of(category), List.of(professional), List.of(schedule)))
            .isNotNull();
    }

    @Test
    void rejectsNonPositiveIds() {
        assertThatIllegalArgumentException().isThrownBy(() -> new CatalogChangedEntities(1,
            List.of(category(0L)), List.of(), List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new CatalogChangedEntities(1,
            List.of(category(-1L)), List.of(), List.of()));
    }

    private ProfessionalCategory category(long id) {
        return new ProfessionalCategory(id, "Clínica", null, true, TIME, TIME);
    }
}
