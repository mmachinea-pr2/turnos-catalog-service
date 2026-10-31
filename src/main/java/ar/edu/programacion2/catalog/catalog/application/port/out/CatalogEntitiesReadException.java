package ar.edu.programacion2.catalog.catalog.application.port.out;

public class CatalogEntitiesReadException extends RuntimeException {
    public CatalogEntitiesReadException() {
        super("Could not read valid catalog entities from Redis");
    }
}
