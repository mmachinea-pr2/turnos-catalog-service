package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalCategoryRepository extends JpaRepository<ProfessionalCategoryEntity, Long> {
    List<ProfessionalCategoryEntity> findAllByEnabledTrueOrderByNameAscIdAsc();
}
