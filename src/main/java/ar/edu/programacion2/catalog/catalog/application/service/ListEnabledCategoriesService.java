package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListEnabledCategories;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogCategoryReader;
import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import java.util.List;

public class ListEnabledCategoriesService implements ListEnabledCategories {
    private final CatalogCategoryReader reader;

    public ListEnabledCategoriesService(CatalogCategoryReader reader) {
        this.reader = reader;
    }

    @Override
    public List<ProfessionalCategory> list() {
        return reader.findEnabled();
    }
}
