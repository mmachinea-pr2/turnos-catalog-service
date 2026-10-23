package ar.edu.programacion2.catalog.catalog.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record WeeklySchedule(Long id, Long professionalId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, int slotDurationMinutes, boolean enabled, Instant createdAt, Instant updatedAt) {
    public WeeklySchedule {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(professionalId, "professionalId is required");
        Objects.requireNonNull(dayOfWeek, "dayOfWeek is required");
        Objects.requireNonNull(startTime, "startTime is required");
        Objects.requireNonNull(endTime, "endTime is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
        if (slotDurationMinutes <= 0) {
            throw new IllegalArgumentException("Slot duration must be positive");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Schedule end must be after start");
        }
    }
}
