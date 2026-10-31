package ar.edu.programacion2.catalog.catalog.infrastructure.web;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalSchedules;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class WeeklyScheduleController {
    private final ListProfessionalSchedules listSchedules;

    public WeeklyScheduleController(ListProfessionalSchedules listSchedules) {
        this.listSchedules = listSchedules;
    }

    @GetMapping("/api/catalog/professionals/{professionalId}/schedules")
    public List<WeeklyScheduleResponse> list(@PathVariable("professionalId") long professionalId) {
        if (professionalId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "professionalId must be positive");
        }
        return listSchedules.list(professionalId).stream()
            .map(schedule -> new WeeklyScheduleResponse(schedule.id(), schedule.professionalId(),
                schedule.dayOfWeek(), schedule.startTime(), schedule.endTime(), schedule.slotDurationMinutes()))
            .toList();
    }
}
