package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationAction;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationStatus;
import ar.edu.programacion2.catalog.catalog.application.model.RemoteCatalogVersions;
import ar.edu.programacion2.catalog.catalog.application.port.in.DetermineCatalogSynchronization;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationStatusReader;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogSynchronizationDecisionConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(CatalogSynchronizationDecisionConfiguration.class);

    @Test
    void disabledByDefaultWithoutExternalDependencies() {
        runner.run(context -> assertThat(context).hasNotFailed()
            .doesNotHaveBean(DetermineCatalogSynchronization.class));
    }

    @Test
    void enablingDoesNotReadUntilExplicitlyInvoked() {
        var reader = mock(CatalogSynchronizationStatusReader.class);
        var source = mock(CatalogVersionSource.class);
        when(reader.read()).thenReturn(new CatalogSynchronizationStatus(4L, null));
        when(source.read()).thenReturn(new RemoteCatalogVersions(7, 4));
        runner.withPropertyValues("catalog.catedra.redis.enabled=true")
            .withBean(CatalogSynchronizationStatusReader.class, () -> reader)
            .withBean(CatalogVersionSource.class, () -> source)
            .run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(DetermineCatalogSynchronization.class);
                verifyNoInteractions(reader, source);
                assertThat(context.getBean(DetermineCatalogSynchronization.class).determine())
                    .isEqualTo(CatalogSynchronizationAction.APPLY_INCREMENTS);
            });
    }
}
