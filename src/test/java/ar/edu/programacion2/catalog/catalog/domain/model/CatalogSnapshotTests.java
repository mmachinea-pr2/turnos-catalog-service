package ar.edu.programacion2.catalog.catalog.domain.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogSnapshotTests {
    private static final Instant TIME = Instant.parse("2026-09-10T13:44:59Z");
    private static final ProfessionalCategory CATEGORY = new ProfessionalCategory(1L, "Clínica", null, false, TIME, TIME);
    private static final Professional PROFESSIONAL = new Professional(1L, 1L, "Ana", "Gómez", false, TIME, TIME);
    private static final WeeklySchedule SCHEDULE = new WeeklySchedule(1L, 1L, DayOfWeek.MONDAY,
        LocalTime.of(9, 0), LocalTime.NOON, 30, false, TIME, TIME);

    @Test
    void acceptsDisabledRecordsAndSameIdInDifferentCollections() {
        var snapshot = new CatalogSnapshot(7, TIME, List.of(CATEGORY), List.of(PROFESSIONAL), List.of(SCHEDULE));
        assertThat(snapshot.professionalCategories()).containsExactly(CATEGORY);
        assertThat(snapshot.professionals()).containsExactly(PROFESSIONAL);
        assertThat(snapshot.weeklySchedules()).containsExactly(SCHEDULE);
        assertThat(snapshot.generatedAt()).isEqualTo(TIME);
        assertThat(snapshot.snapshotVersion()).isEqualTo(7);
    }

    @Test
    void allowsEmptyCatalogAndVersionZero() {
        var snapshot = new CatalogSnapshot(0, TIME, List.of(), List.of(), List.of());
        assertThat(snapshot.professionals()).isEmpty();
        assertThat(snapshot.snapshotVersion()).isZero();
    }

    @Test
    void rejectsNegativeVersionAndMissingGenerationTime() {
        assertThatThrownBy(() -> new CatalogSnapshot(-1, TIME, List.of(), List.of(), List.of()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CatalogSnapshot(0, null, List.of(), List.of(), List.of()))
            .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void rejectsDuplicateIdsInEachCollection(int collection) {
        var categories = collection == 0 ? List.of(CATEGORY, CATEGORY) : List.of(CATEGORY);
        var professionals = collection == 1 ? List.of(PROFESSIONAL, PROFESSIONAL) : List.of(PROFESSIONAL);
        var schedules = collection == 2 ? List.of(SCHEDULE, SCHEDULE) : List.of(SCHEDULE);
        assertThatThrownBy(() -> new CatalogSnapshot(7, TIME, categories, professionals, schedules))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Duplicate");
    }

    @Test
    void rejectsBrokenReferencesEvenForDisabledRecords() {
        assertThatThrownBy(() -> new CatalogSnapshot(7, TIME, List.of(), List.of(PROFESSIONAL), List.of()))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("missing category");
        assertThatThrownBy(() -> new CatalogSnapshot(7, TIME, List.of(CATEGORY), List.of(), List.of(SCHEDULE)))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("missing professional");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void rejectsMissingCollectionsAndNullMembers(int collection) {
        List<ProfessionalCategory> categories = collection == 0 ? null : List.of(CATEGORY);
        List<Professional> professionals = collection == 1 ? null : List.of(PROFESSIONAL);
        List<WeeklySchedule> schedules = collection == 2 ? null : List.of(SCHEDULE);
        assertThatThrownBy(() -> new CatalogSnapshot(7, TIME, categories, professionals, schedules))
            .isInstanceOf(NullPointerException.class);
        var categoriesWithNull = collection == 0 ? Arrays.asList(CATEGORY, null) : List.of(CATEGORY);
        var professionalsWithNull = collection == 1 ? Arrays.asList(PROFESSIONAL, null) : List.of(PROFESSIONAL);
        var schedulesWithNull = collection == 2 ? Arrays.asList(SCHEDULE, null) : List.of(SCHEDULE);
        assertThatThrownBy(() -> new CatalogSnapshot(7, TIME, categoriesWithNull, professionalsWithNull, schedulesWithNull))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void cannotBeChangedThroughSourceListsOrAccessors() {
        var categories = new ArrayList<>(List.of(CATEGORY));
        var professionals = new ArrayList<>(List.of(PROFESSIONAL));
        var schedules = new ArrayList<>(List.of(SCHEDULE));
        var snapshot = new CatalogSnapshot(7, TIME, categories, professionals, schedules);
        categories.clear();
        professionals.clear();
        schedules.clear();
        assertThat(snapshot.professionalCategories()).containsExactly(CATEGORY);
        assertThat(snapshot.professionals()).containsExactly(PROFESSIONAL);
        assertThat(snapshot.weeklySchedules()).containsExactly(SCHEDULE);
        assertThatThrownBy(() -> snapshot.professionalCategories().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snapshot.professionals().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snapshot.weeklySchedules().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
