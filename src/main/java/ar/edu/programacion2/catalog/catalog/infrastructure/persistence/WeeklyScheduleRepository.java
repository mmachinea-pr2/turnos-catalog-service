package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeeklyScheduleRepository extends JpaRepository<WeeklyScheduleEntity, Long> {
    @Query("""
        select schedule from WeeklyScheduleEntity schedule
        join ProfessionalEntity professional on professional.id = schedule.professionalId
        join ProfessionalCategoryEntity category on category.id = professional.categoryId
        where professional.id = :professionalId
          and schedule.enabled = true and professional.enabled = true and category.enabled = true
        """)
    List<WeeklyScheduleEntity> findEnabledByProfessional(@Param("professionalId") long professionalId);
}
