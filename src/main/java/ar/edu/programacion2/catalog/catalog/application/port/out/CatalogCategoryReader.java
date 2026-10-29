package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import java.util.List;

public interface CatalogCategoryReader {
    /** Categorías habilitadas, ordenadas por nombre y luego por ID. */
    List<ProfessionalCategory> findEnabled();
}
