package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.in.DetermineCatalogSynchronization;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationStatusReader;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionSource;
import ar.edu.programacion2.catalog.catalog.application.service.DetermineCatalogSynchronizationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "catalog.catedra.redis.enabled", havingValue = "true")
public class CatalogSynchronizationDecisionConfiguration {
    @Bean
    DetermineCatalogSynchronization determineCatalogSynchronization(
            CatalogSynchronizationStatusReader reader, CatalogVersionSource source) {
        return new DetermineCatalogSynchronizationService(reader, source);
    }
}
