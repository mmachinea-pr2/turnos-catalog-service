package ar.edu.programacion2.catalog.catalog.application.port.out;

public class CatalogIncrementRejectedException extends RuntimeException {
    public CatalogIncrementRejectedException(String message) {
        super(message);
    }
}
