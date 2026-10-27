package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.domain.model.CatalogSnapshot;

/** Persiste el snapshot y su versión como una unidad indivisible. */
public interface CatalogSnapshotStore {
    /** Devuelve false cuando la versión ya fue aplicada o es anterior a la local. */
    boolean apply(CatalogSnapshot snapshot);
}
