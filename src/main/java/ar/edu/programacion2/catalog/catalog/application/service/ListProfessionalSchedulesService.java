package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalSchedules;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogScheduleReader;
import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.util.Comparator;
import java.util.List;

public class ListProfessionalSchedulesService implements ListProfessionalSchedules {
    private final CatalogScheduleReader reader;

    public ListProfessionalSchedulesService(CatalogScheduleReader reader) {
        this.reader = reader;
    }

    @Override
    public List<WeeklySchedule> list(long professionalId) {
        return reader.findEnabledByProfessional(professionalId).stream()
            .sorted(Comparator.comparing(WeeklySchedule::dayOfWeek)
                .thenComparing(WeeklySchedule::startTime)
                .thenComparing(WeeklySchedule::endTime)
                .thenComparing(WeeklySchedule::id))
            .toList();
    }
}
