package ar.edu.programacion2.catalog.catalog.application.port.in;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogIncrementResult;

public interface SynchronizeCatalogIncrement {
    CatalogIncrementResult synchronize();
}
