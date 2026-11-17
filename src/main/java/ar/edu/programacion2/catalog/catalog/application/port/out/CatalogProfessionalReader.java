package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.application.model.PageQuery;
import ar.edu.programacion2.catalog.catalog.application.model.ProfessionalPage;

public interface CatalogProfessionalReader {
    /** Categoría habilitada y profesionales del estado solicitado; coincidencia literal en nombre completo y orden por apellido, nombre e ID. */
    ProfessionalPage findByCategoryAndEnabled(long categoryId, String normalizedName, java.time.DayOfWeek dayOfWeek, boolean enabled, PageQuery page);
}
