package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.util.List;

public interface CatalogScheduleReader {
    /** Horarios habilitados de un profesional y categoría también habilitados. */
    List<WeeklySchedule> findEnabledByProfessional(long professionalId);
}
