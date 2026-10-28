package ar.edu.programacion2.catalog.catalog.application.port.in;

public interface SynchronizeCatalogSnapshot {
    /** Devuelve true si se aplicó una nueva versión; false si ya estaba cubierta. */
    boolean synchronize();
}
