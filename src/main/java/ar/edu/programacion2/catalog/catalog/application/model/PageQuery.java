package ar.edu.programacion2.catalog.catalog.application.model;

/** Parámetros de paginación independientes de Spring Data. */
public record PageQuery(int page, int size) {
    public PageQuery {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid page or size");
        }
    }
}
