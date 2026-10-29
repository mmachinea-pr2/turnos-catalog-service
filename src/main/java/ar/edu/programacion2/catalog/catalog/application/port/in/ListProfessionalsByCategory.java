package ar.edu.programacion2.catalog.catalog.application.port.in;

import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import java.util.List;

public interface ListProfessionalsByCategory {
    List<Professional> list(long categoryId);
}
