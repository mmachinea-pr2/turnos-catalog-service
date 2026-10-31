package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.application.model.RemoteCatalogVersions;

public interface CatalogVersionSource {
    RemoteCatalogVersions read();
}
