package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalsByCategory;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogProfessionalReader;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import java.util.List;

public class ListProfessionalsByCategoryService implements ListProfessionalsByCategory {
    private final CatalogProfessionalReader reader;

    public ListProfessionalsByCategoryService(CatalogProfessionalReader reader) {
        this.reader = reader;
    }

    @Override
    public List<Professional> list(long categoryId) {
        return reader.findEnabledByCategory(categoryId);
    }
}
