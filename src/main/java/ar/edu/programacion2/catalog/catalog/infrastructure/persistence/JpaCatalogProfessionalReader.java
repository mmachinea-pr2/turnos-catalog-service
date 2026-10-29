package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogProfessionalReader;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCatalogProfessionalReader implements CatalogProfessionalReader {
    private final ProfessionalRepository professionals;

    public JpaCatalogProfessionalReader(ProfessionalRepository professionals) {
        this.professionals = professionals;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Professional> findEnabledByCategory(long categoryId) {
        return professionals.findEnabledByCategory(categoryId).stream()
            .map(CatalogPersistenceMapper::toDomain).toList();
    }
}
