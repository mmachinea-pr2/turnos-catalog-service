package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotStore;
import ar.edu.programacion2.catalog.catalog.domain.model.CatalogSnapshot;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogSynchronizationConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(CatalogSynchronizationConfiguration.class);

    @Test
    void disabledConfigurationNeedsNoExternalClient() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(SynchronizeCatalogSnapshot.class));
    }

    @Test
    void createsUseCaseWithoutPerformingSynchronizationOnStartup() {
        var source = mock(CatalogSnapshotSource.class);
        var store = mock(CatalogSnapshotStore.class);
        var snapshot = new CatalogSnapshot(0, Instant.EPOCH, List.of(), List.of(), List.of());
        when(source.fetch()).thenReturn(snapshot);
        when(store.apply(snapshot)).thenReturn(true);
        runner.withPropertyValues("catalog.catedra.enabled=true")
            .withBean(CatalogSnapshotSource.class, () -> source)
            .withBean(CatalogSnapshotStore.class, () -> store)
            .run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(SynchronizeCatalogSnapshot.class);
                verifyNoInteractions(source, store);
                assertThat(context.getBean(SynchronizeCatalogSnapshot.class).synchronize()).isTrue();
                var order = inOrder(source, store);
                order.verify(source).fetch();
                order.verify(store).apply(snapshot);
                order.verifyNoMoreInteractions();
            });
    }
}
