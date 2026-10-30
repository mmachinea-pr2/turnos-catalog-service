package ar.edu.programacion2.catalog.catalog.application.port.in;

import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.util.List;

public interface ListProfessionalSchedules {
    List<WeeklySchedule> list(long professionalId);
}
