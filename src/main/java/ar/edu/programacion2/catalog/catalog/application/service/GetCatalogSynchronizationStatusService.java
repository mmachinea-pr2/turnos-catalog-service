package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationStatus;
import ar.edu.programacion2.catalog.catalog.application.port.in.GetCatalogSynchronizationStatus;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationStatusReader;

public class GetCatalogSynchronizationStatusService implements GetCatalogSynchronizationStatus {
    private final CatalogSynchronizationStatusReader reader;

    public GetCatalogSynchronizationStatusService(CatalogSynchronizationStatusReader reader) {
        this.reader = reader;
    }

    @Override
    public CatalogSynchronizationStatus get() {
        return reader.read();
    }
}
