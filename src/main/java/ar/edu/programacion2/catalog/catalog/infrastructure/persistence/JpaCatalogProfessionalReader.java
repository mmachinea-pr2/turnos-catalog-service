package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogProfessionalReader;
import ar.edu.programacion2.catalog.catalog.application.model.PageQuery;
import ar.edu.programacion2.catalog.catalog.application.model.ProfessionalPage;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCatalogProfessionalReader implements CatalogProfessionalReader {
    private final ProfessionalRepository professionals;

    public JpaCatalogProfessionalReader(ProfessionalRepository professionals) {
        this.professionals = professionals;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessionalPage findEnabledByCategory(long categoryId, String normalizedName, PageQuery page) {
        var result = professionals.findEnabledByCategory(categoryId, normalizedName,
            PageRequest.of(page.page(), page.size()));
        return new ProfessionalPage(result.getContent().stream()
            .map(CatalogPersistenceMapper::toDomain).toList(), result.getTotalElements());
    }
}
