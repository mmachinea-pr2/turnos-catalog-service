package ar.edu.programacion2.catalog.catalog.infrastructure.web;

import ar.edu.programacion2.catalog.catalog.application.port.in.GetCatalogSynchronizationStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogSynchronizationStatusController {
    private final GetCatalogSynchronizationStatus getStatus;

    private final ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationAttemptStore attempts;

    public CatalogSynchronizationStatusController(GetCatalogSynchronizationStatus getStatus,
            ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationAttemptStore attempts) {
        this.attempts = attempts;
        this.getStatus = getStatus;
    }

    @GetMapping("/api/catalog/synchronization/status")
    public CatalogSynchronizationStatusResponse get() {
        var status = getStatus.get();
        return new CatalogSynchronizationStatusResponse(status.initialized(), status.appliedVersion(), status.appliedAt(), attempts.read());
    }
}
