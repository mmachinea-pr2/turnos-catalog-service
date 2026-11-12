package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationAttemptStore;
import ar.edu.programacion2.catalog.catalog.application.service.TrackCatalogSynchronization;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CatalogAttemptConfiguration {
    @Bean
    TrackCatalogSynchronization trackCatalogSynchronization(CatalogSynchronizationAttemptStore store) {
        return new TrackCatalogSynchronization(store, Clock.systemUTC());
    }
}
