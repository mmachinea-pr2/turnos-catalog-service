package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChangedEntities;

public interface CatalogIncrementStore {
    /** Applies the next version atomically; false means it was already covered locally. */
    boolean apply(CatalogChangedEntities changes);
}
