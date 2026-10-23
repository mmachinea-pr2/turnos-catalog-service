package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogPersistenceMapperTests {
    private static final Instant CREATED = Instant.parse("2026-09-10T13:40:00.123456Z");
    private static final Instant UPDATED = CREATED.plusSeconds(60);

    @Test
    void preservesAllCategoryFieldsIncludingNullableDescription() {
        var model = new ProfessionalCategory(41L, "Clínica", null, false, CREATED, UPDATED);
        assertThat(CatalogPersistenceMapper.toDomain(CatalogPersistenceMapper.toEntity(model))).isEqualTo(model);
    }

    @Test
    void preservesProfessionalIdentityReferenceAndAccents() {
        var model = new Professional(72L, 41L, "Ana María", "Gómez", false, CREATED, UPDATED);
        assertThat(CatalogPersistenceMapper.toDomain(CatalogPersistenceMapper.toEntity(model))).isEqualTo(model);
    }

    @Test
    void preservesScheduleLocalTimesAndDuration() {
        var model = new WeeklySchedule(93L, 72L, DayOfWeek.FRIDAY,
            LocalTime.of(9, 15, 30), LocalTime.of(12, 45, 30), 25, false, CREATED, UPDATED);
        assertThat(CatalogPersistenceMapper.toDomain(CatalogPersistenceMapper.toEntity(model))).isEqualTo(model);
    }

    @Test
    void invalidPersistedScheduleCannotBecomeValidDomainModel() {
        var entity = new WeeklyScheduleEntity(93L, 72L, DayOfWeek.FRIDAY,
            LocalTime.NOON, LocalTime.of(9, 0), 30, true, CREATED, UPDATED);
        assertThatThrownBy(() -> CatalogPersistenceMapper.toDomain(entity)).isInstanceOf(IllegalArgumentException.class);
    }
}
