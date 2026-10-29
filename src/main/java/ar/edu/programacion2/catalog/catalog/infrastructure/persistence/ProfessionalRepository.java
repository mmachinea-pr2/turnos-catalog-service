package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessionalRepository extends JpaRepository<ProfessionalEntity, Long> {
    @Query("""
        select professional from ProfessionalEntity professional
        join ProfessionalCategoryEntity category on category.id = professional.categoryId
        where category.id = :categoryId and category.enabled = true and professional.enabled = true
        order by professional.lastName asc, professional.firstName asc, professional.id asc
        """)
    List<ProfessionalEntity> findEnabledByCategory(@Param("categoryId") long categoryId);
}
