package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogIncrement;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogRecoveryConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
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
        runner.withPropertyValues("catalog.catedra.enabled=true", "catalog.catedra.redis.enabled=true")
            .withBean(SynchronizeCatalogIncrement.class, () -> increment)
            .withBean(SynchronizeCatalogSnapshot.class, () -> snapshot)
            .run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(SynchronizeCatalog.class);
                verifyNoInteractions(increment, snapshot);
            });
    }
}
