package ar.edu.programacion2.catalog.catalog.infrastructure.scheduling;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "catalog.synchronization.periodic.enabled", havingValue = "true")
public class PeriodicCatalogConfiguration {
    @Bean
    CatalogSynchronizationSchedule catalogSynchronizationSchedule(SynchronizeCatalog synchronize,
            @Value("${catalog.synchronization.periodic.interval:60s}") String interval,
            @Value("${catalog.synchronization.periodic.initial-delay:5s}") String initialDelay,
            @Value("${catalog.synchronization.run-on-startup:false}") boolean manualSnapshot,
            @Value("${catalog.synchronization.full-run-on-startup:false}") boolean manualFull) {
        if (manualSnapshot || manualFull) {
            throw new IllegalArgumentException("Periodic synchronization cannot run with manual synchronization modes");
        }
        return new CatalogSynchronizationSchedule(new PeriodicCatalogSynchronization(synchronize),
                DurationStyle.detectAndParse(interval), DurationStyle.detectAndParse(initialDelay));
    }
}
