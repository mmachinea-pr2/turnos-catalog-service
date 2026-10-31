package ar.edu.programacion2.catalog.catalog.application.port.out;

public class CatalogChangesReadException extends RuntimeException {
    public CatalogChangesReadException() {
        super("Could not read valid catalog changes from Redis");
    }
}
