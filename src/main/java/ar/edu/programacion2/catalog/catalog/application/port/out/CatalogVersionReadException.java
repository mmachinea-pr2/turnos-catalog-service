package ar.edu.programacion2.catalog.catalog.application.port.out;

public class CatalogVersionReadException extends RuntimeException {
    public CatalogVersionReadException() {
        super("Could not read valid catalog versions from Redis");
    }
}
