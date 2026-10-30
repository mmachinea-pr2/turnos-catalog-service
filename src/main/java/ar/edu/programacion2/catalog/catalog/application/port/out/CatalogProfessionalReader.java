package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.application.model.PageQuery;
import ar.edu.programacion2.catalog.catalog.application.model.ProfessionalPage;

public interface CatalogProfessionalReader {
    /** Solo profesionales y categoría habilitados; coincidencia literal en nombre completo y orden por apellido, nombre e ID. */
    ProfessionalPage findEnabledByCategory(long categoryId, String normalizedName, PageQuery page);
}
