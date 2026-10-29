package ar.edu.programacion2.catalog.catalog.application.port.in;

import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import java.util.List;

public interface ListEnabledCategories {
    List<ProfessionalCategory> list();
}
