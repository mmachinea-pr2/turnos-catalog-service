package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotStore;
import ar.edu.programacion2.catalog.catalog.application.service.SynchronizeCatalogSnapshotService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "catalog.catedra.enabled", havingValue = "true")
public class CatalogSynchronizationConfiguration {
    @Bean
    SynchronizeCatalogSnapshot synchronizeCatalogSnapshot(CatalogSnapshotSource source, CatalogSnapshotStore store,
            ar.edu.programacion2.catalog.catalog.application.service.TrackCatalogSynchronization tracker) {
        var delegate = new SynchronizeCatalogSnapshotService(source, store);
        return () -> tracker.execute(delegate::synchronize,
            applied -> applied ? "SNAPSHOT_APPLIED" : "SNAPSHOT_ALREADY_COVERED");
    }
}
