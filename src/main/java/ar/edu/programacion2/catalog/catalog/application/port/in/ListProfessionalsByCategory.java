package ar.edu.programacion2.catalog.catalog.application.port.in;

import ar.edu.programacion2.catalog.catalog.application.model.PageQuery;
import ar.edu.programacion2.catalog.catalog.application.model.ProfessionalPage;

public interface ListProfessionalsByCategory {
    ProfessionalPage list(long categoryId, String name, PageQuery page);
}
