package ar.edu.programacion2.catalog.catalog.application.port.in;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogUpdatedEvent;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogEventResult;

public interface HandleCatalogUpdated {
    CatalogEventResult handle(CatalogUpdatedEvent event);
}
