package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogCategoryReader;
import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCatalogCategoryReader implements CatalogCategoryReader {
    private final ProfessionalCategoryRepository categories;

    public JpaCatalogCategoryReader(ProfessionalCategoryRepository categories) {
        this.categories = categories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessionalCategory> findEnabled() {
        return categories.findAllByEnabledTrueOrderByNameAscIdAsc().stream()
            .map(CatalogPersistenceMapper::toDomain).toList();
    }
}
