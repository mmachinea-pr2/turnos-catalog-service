package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessionalRepository extends JpaRepository<ProfessionalEntity, Long> {
    @Query("""
        select professional from ProfessionalEntity professional
        join ProfessionalCategoryEntity category on category.id = professional.categoryId
        where category.id = :categoryId and category.enabled = true and professional.enabled = true
        and locate(:name, lower(concat(professional.firstName, ' ', professional.lastName))) > 0
        order by professional.lastName asc, professional.firstName asc, professional.id asc
        """)
    Page<ProfessionalEntity> findEnabledByCategory(@Param("categoryId") long categoryId, @Param("name") String normalizedName, Pageable pageable);
}
