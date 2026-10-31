package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalsByCategory;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogProfessionalReader;
import ar.edu.programacion2.catalog.catalog.application.model.PageQuery;
import ar.edu.programacion2.catalog.catalog.application.model.ProfessionalPage;
import java.util.Locale;

public class ListProfessionalsByCategoryService implements ListProfessionalsByCategory {
    private final CatalogProfessionalReader reader;

    public ListProfessionalsByCategoryService(CatalogProfessionalReader reader) {
        this.reader = reader;
    }

    @Override
    public ProfessionalPage list(long categoryId, String name, PageQuery page) {
        String normalizedName = name == null ? "" : name.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return reader.findEnabledByCategory(categoryId, normalizedName, page);
    }
}
