package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogIncrement;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import ar.edu.programacion2.catalog.catalog.application.service.SynchronizeCatalogService;
import ar.edu.programacion2.catalog.catalog.application.port.in.HandleCatalogUpdated;
import ar.edu.programacion2.catalog.catalog.application.port.out.ProcessedCatalogEventStore;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationStatusReader;
import ar.edu.programacion2.catalog.catalog.application.service.HandleCatalogUpdatedService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = {"catalog.catedra.enabled", "catalog.catedra.redis.enabled"}, havingValue = "true")
public class CatalogRecoveryConfiguration {
    @Bean
    HandleCatalogUpdated handleCatalogUpdated(ProcessedCatalogEventStore events,
            CatalogSynchronizationStatusReader status, SynchronizeCatalog synchronize) {
        return new HandleCatalogUpdatedService(events, status, synchronize);
    }

    @Bean
    SynchronizeCatalog synchronizeCatalog(SynchronizeCatalogIncrement increment, SynchronizeCatalogSnapshot snapshot) {
        return new SynchronizeCatalogService(increment, snapshot);
    }
}
