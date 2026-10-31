package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import java.util.List;

public interface CatalogProfessionalReader {
    /** Solo profesionales y categoría habilitados; coincidencia literal en nombre completo y orden por apellido, nombre e ID. */
    List<Professional> findEnabledByCategory(long categoryId, String normalizedName);
}
