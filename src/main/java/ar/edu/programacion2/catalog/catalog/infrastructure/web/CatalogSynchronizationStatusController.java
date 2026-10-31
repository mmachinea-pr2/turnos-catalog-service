package ar.edu.programacion2.catalog.catalog.infrastructure.web;

import ar.edu.programacion2.catalog.catalog.application.port.in.GetCatalogSynchronizationStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogSynchronizationStatusController {
    private final GetCatalogSynchronizationStatus getStatus;

    public CatalogSynchronizationStatusController(GetCatalogSynchronizationStatus getStatus) {
        this.getStatus = getStatus;
    }

    @GetMapping("/api/catalog/synchronization/status")
    public CatalogSynchronizationStatusResponse get() {
        var status = getStatus.get();
        return new CatalogSynchronizationStatusResponse(status.initialized(), status.appliedVersion(), status.appliedAt());
    }
}
