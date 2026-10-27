package ar.edu.programacion2.catalog.catalog.infrastructure.rest;

import ar.edu.programacion2.catalog.catalog.domain.model.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
record SnapshotResponse(Long snapshotVersion, Instant generatedAt,
        List<CategoryResponse> professionalCategories, List<ProfessionalResponse> professionals,
        List<ScheduleResponse> weeklySchedules) {
    CatalogSnapshot toDomain() {
        return new CatalogSnapshot(Objects.requireNonNull(snapshotVersion), generatedAt,
            professionalCategories.stream().map(CategoryResponse::toDomain).toList(),
            professionals.stream().map(ProfessionalResponse::toDomain).toList(),
            weeklySchedules.stream().map(ScheduleResponse::toDomain).toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CategoryResponse(Long id, String name, String description, Boolean enabled,
            Instant createdAt, Instant updatedAt) {
        ProfessionalCategory toDomain() {
            return new ProfessionalCategory(id, name, description, Objects.requireNonNull(enabled), createdAt, updatedAt);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProfessionalResponse(Long id, Long categoryId, String firstName, String lastName,
            Boolean enabled, Instant createdAt, Instant updatedAt) {
        Professional toDomain() {
            return new Professional(id, categoryId, firstName, lastName, Objects.requireNonNull(enabled), createdAt, updatedAt);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ScheduleResponse(Long id, Long professionalId, DayOfWeek dayOfWeek, LocalTime startTime,
            LocalTime endTime, Integer slotDurationMinutes, Boolean enabled, Instant createdAt, Instant updatedAt) {
        WeeklySchedule toDomain() {
            return new WeeklySchedule(id, professionalId, dayOfWeek, startTime, endTime,
                Objects.requireNonNull(slotDurationMinutes), Objects.requireNonNull(enabled), createdAt, updatedAt);
        }
    }
}
