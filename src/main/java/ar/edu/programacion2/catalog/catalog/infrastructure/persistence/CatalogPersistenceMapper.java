package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.util.Objects;

/** Traduce datos sin modificar IDs, bajas lógicas, horarios o fechas de origen. */
public final class CatalogPersistenceMapper {
    private CatalogPersistenceMapper() {}

    public static ProfessionalCategory toDomain(ProfessionalCategoryEntity entity) {
        Objects.requireNonNull(entity, "entity is required");
        return new ProfessionalCategory(entity.getId(), entity.getName(), entity.getDescription(), entity.getEnabled(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public static ProfessionalCategoryEntity toEntity(ProfessionalCategory model) {
        Objects.requireNonNull(model, "model is required");
        return new ProfessionalCategoryEntity(model.id(), model.name(), model.description(), model.enabled(), model.createdAt(), model.updatedAt());
    }

    public static Professional toDomain(ProfessionalEntity entity) {
        Objects.requireNonNull(entity, "entity is required");
        return new Professional(entity.getId(), entity.getCategoryId(), entity.getFirstName(), entity.getLastName(), entity.getEnabled(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public static ProfessionalEntity toEntity(Professional model) {
        Objects.requireNonNull(model, "model is required");
        return new ProfessionalEntity(model.id(), model.categoryId(), model.firstName(), model.lastName(), model.enabled(), model.createdAt(), model.updatedAt());
    }

    public static WeeklySchedule toDomain(WeeklyScheduleEntity entity) {
        Objects.requireNonNull(entity, "entity is required");
        return new WeeklySchedule(entity.getId(), entity.getProfessionalId(), entity.getDayOfWeek(), entity.getStartTime(), entity.getEndTime(), entity.getSlotDurationMinutes(), entity.getEnabled(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public static WeeklyScheduleEntity toEntity(WeeklySchedule model) {
        Objects.requireNonNull(model, "model is required");
        return new WeeklyScheduleEntity(model.id(), model.professionalId(), model.dayOfWeek(), model.startTime(), model.endTime(), model.slotDurationMinutes(), model.enabled(), model.createdAt(), model.updatedAt());
    }
}
