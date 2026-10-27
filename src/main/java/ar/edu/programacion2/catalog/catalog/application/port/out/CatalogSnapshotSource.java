package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.domain.model.CatalogSnapshot;

public interface CatalogSnapshotSource {
    CatalogSnapshot fetch();
}
