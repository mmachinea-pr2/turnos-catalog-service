package ar.edu.programacion2.catalog.catalog.domain.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogDomainTests {
    private static final Instant CREATED = Instant.parse("2026-09-10T13:40:00Z");

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -30})
    void rejectsNonPositiveSlotDuration(int minutes) {
        assertThatThrownBy(() -> schedule(LocalTime.of(9, 0), LocalTime.of(12, 0), minutes))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyAndReversedIntervals() {
        assertThatThrownBy(() -> schedule(LocalTime.NOON, LocalTime.NOON, 30))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> schedule(LocalTime.NOON, LocalTime.of(9, 0), 30))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsPartialLastIntervalWithoutInventingDivisibilityRule() {
        assertThat(schedule(LocalTime.of(9, 0), LocalTime.of(10, 10), 30).endTime())
            .isEqualTo(LocalTime.of(10, 10));
    }

    @Test
    void disabledCategoryCanHaveNoDescription() {
        var category = new ProfessionalCategory(1L, "Clínica", null, false, CREATED, CREATED);
        assertThat(category.enabled()).isFalse();
        assertThat(category.description()).isNull();
    }

    @Test
    void rejectsMissingReferencesAndRequiredFields() {
        assertThatThrownBy(() -> new Professional(2L, null, "Ana", "Gómez", true, CREATED, CREATED))
            .isInstanceOf(NullPointerException.class).hasMessageContaining("categoryId");
        assertThatThrownBy(() -> new ProfessionalCategory(null, "Clínica", null, true, CREATED, CREATED))
            .isInstanceOf(NullPointerException.class).hasMessageContaining("id");
        assertThatThrownBy(() -> new WeeklySchedule(3L, 2L, null, LocalTime.of(9, 0), LocalTime.NOON, 30, true, CREATED, CREATED))
            .isInstanceOf(NullPointerException.class).hasMessageContaining("dayOfWeek");
    }

    private WeeklySchedule schedule(LocalTime start, LocalTime end, int minutes) {
        return new WeeklySchedule(3L, 2L, DayOfWeek.MONDAY, start, end, minutes, false, CREATED, CREATED);
    }
}
