package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogScheduleReader;
import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCatalogScheduleReader implements CatalogScheduleReader {
    private final WeeklyScheduleRepository schedules;

    public JpaCatalogScheduleReader(WeeklyScheduleRepository schedules) {
        this.schedules = schedules;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeeklySchedule> findEnabledByProfessional(long professionalId) {
        return schedules.findEnabledByProfessional(professionalId).stream()
            .map(CatalogPersistenceMapper::toDomain).toList();
    }
}
