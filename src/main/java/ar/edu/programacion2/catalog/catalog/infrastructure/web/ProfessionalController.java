package ar.edu.programacion2.catalog.catalog.infrastructure.web;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalsByCategory;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ProfessionalController {
    private final ListProfessionalsByCategory listProfessionals;

    public ProfessionalController(ListProfessionalsByCategory listProfessionals) {
        this.listProfessionals = listProfessionals;
    }

    @GetMapping("/api/catalog/professionals")
    public List<ProfessionalResponse> list(@RequestParam("categoryId") long categoryId) {
        if (categoryId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId must be positive");
        }
        return listProfessionals.list(categoryId).stream()
            .map(professional -> new ProfessionalResponse(professional.id(), professional.categoryId(),
                professional.firstName(), professional.lastName()))
            .toList();
    }
}
