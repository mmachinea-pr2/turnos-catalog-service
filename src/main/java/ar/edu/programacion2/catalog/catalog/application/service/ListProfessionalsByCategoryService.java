package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalsByCategory;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogProfessionalReader;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import java.util.List;
import java.util.Locale;

public class ListProfessionalsByCategoryService implements ListProfessionalsByCategory {
    private final CatalogProfessionalReader reader;

    public ListProfessionalsByCategoryService(CatalogProfessionalReader reader) {
        this.reader = reader;
    }

    @Override
    public List<Professional> list(long categoryId, String name) {
        String normalizedName = name == null ? "" : name.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return reader.findEnabledByCategory(categoryId, normalizedName);
    }
}
