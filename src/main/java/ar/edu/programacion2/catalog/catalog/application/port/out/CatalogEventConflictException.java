package ar.edu.programacion2.catalog.catalog.application.port.out;

public class CatalogEventConflictException extends RuntimeException {
    public CatalogEventConflictException() {
        super("Catalog event ID was already used for another version");
    }
}
