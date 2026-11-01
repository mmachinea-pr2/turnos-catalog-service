package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChanges;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogChangedEntities;

public interface CatalogEntitiesSource {
    CatalogChangedEntities read(CatalogChanges changes);
}
