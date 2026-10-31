package ar.edu.programacion2.catalog.catalog.infrastructure.web;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WeeklyScheduleResponse(Long id, Long professionalId, DayOfWeek dayOfWeek,
        LocalTime startTime, LocalTime endTime, int slotDurationMinutes) {}
