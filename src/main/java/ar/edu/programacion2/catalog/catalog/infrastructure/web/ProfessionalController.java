package ar.edu.programacion2.catalog.catalog.infrastructure.web;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalsByCategory;
import java.util.List;
import ar.edu.programacion2.catalog.catalog.application.model.PageQuery;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ProfessionalResponse>> list(@RequestParam("categoryId") long categoryId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        if (categoryId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryId must be positive");
        }
        final PageQuery pagination;
        try {
            pagination = new PageQuery(page, size);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page or size");
        }
        var result = listProfessionals.list(categoryId, name, pagination);
        var body = result.content().stream()
            .map(professional -> new ProfessionalResponse(professional.id(), professional.categoryId(),
                professional.firstName(), professional.lastName()))
            .toList();
        return ResponseEntity.ok().header("X-Total-Count", Long.toString(result.totalElements())).body(body);
    }
}
