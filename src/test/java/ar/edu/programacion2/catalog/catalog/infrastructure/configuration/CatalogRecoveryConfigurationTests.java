package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogIncrement;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import ar.edu.programacion2.catalog.catalog.application.port.in.HandleCatalogUpdated;
import ar.edu.programacion2.catalog.catalog.application.port.out.ProcessedCatalogEventStore;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationStatusReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogRecoveryConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withBean(ar.edu.programacion2.catalog.catalog.application.service.TrackCatalogSynchronization.class,
            () -> new ar.edu.programacion2.catalog.catalog.application.service.TrackCatalogSynchronization(
                mock(ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationAttemptStore.class),
                java.time.Clock.systemUTC()))
        .withUserConfiguration(CatalogRecoveryConfiguration.class);

    @Test
    void disabledByDefault() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(SynchronizeCatalog.class));
    }

    @ParameterizedTest
    @CsvSource({"true,false", "false,true", "false,false"})
    void bothClientsAreRequired(boolean rest, boolean redis) {
        runner.withPropertyValues("catalog.catedra.enabled=" + rest, "catalog.catedra.redis.enabled=" + redis)
            .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(SynchronizeCatalog.class));
    }

    @Test
    void enablingDoesNotStartSynchronization() {
        var increment = mock(SynchronizeCatalogIncrement.class);
        var snapshot = mock(SynchronizeCatalogSnapshot.class);
        var events = mock(ProcessedCatalogEventStore.class);
        var status = mock(CatalogSynchronizationStatusReader.class);
        runner.withPropertyValues("catalog.catedra.enabled=true", "catalog.catedra.redis.enabled=true")
            .withBean(SynchronizeCatalogIncrement.class, () -> increment)
            .withBean(SynchronizeCatalogSnapshot.class, () -> snapshot)
            .withBean(ProcessedCatalogEventStore.class, () -> events)
            .withBean(CatalogSynchronizationStatusReader.class, () -> status)
            .run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(SynchronizeCatalog.class).hasSingleBean(HandleCatalogUpdated.class);
                verifyNoInteractions(increment, snapshot, events, status);
                when(increment.synchronize()).thenReturn(ar.edu.programacion2.catalog.catalog.application.model.CatalogIncrementResult.UP_TO_DATE);
                assertThat(context.getBean(SynchronizeCatalog.class).synchronize())
                    .isEqualTo(ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult.UP_TO_DATE);
            });
    }
}
