package ar.edu.programacion2.catalog.catalog.infrastructure.web;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListEnabledCategories;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    private final ListEnabledCategories listCategories;

    public CategoryController(ListEnabledCategories listCategories) {
        this.listCategories = listCategories;
    }

    @GetMapping("/api/catalog/categories")
    public List<CategoryResponse> list() {
        return listCategories.list().stream()
            .map(category -> new CategoryResponse(category.id(), category.name(), category.description()))
            .toList();
    }
}
