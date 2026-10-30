package ar.edu.programacion2.catalog.catalog.application.model;

import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import java.util.List;

public record ProfessionalPage(List<Professional> content, long totalElements) {
    public ProfessionalPage {
        content = List.copyOf(content);
    }
}
