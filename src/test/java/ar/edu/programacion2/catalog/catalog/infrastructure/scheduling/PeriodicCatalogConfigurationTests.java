package ar.edu.programacion2.catalog.catalog.infrastructure.scheduling;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PeriodicCatalogConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(PeriodicCatalogConfiguration.class);

    @Test
    void disabledRequiresNoRemoteClients() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(CatalogSynchronizationSchedule.class));
    }

    @Test
    void enabledRequiresSynchronizationDependencies() {
        runner.withPropertyValues("catalog.synchronization.periodic.enabled=true")
            .run(context -> assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"interval=0s", "interval=-1s", "interval=999ms", "interval=2d", "interval=invalid",
        "initial-delay=-1s", "initial-delay=2d", "initial-delay=invalid"})
    void invalidTimingFailsAtStartup(String setting) {
        configured(mock(SynchronizeCatalog.class))
            .withPropertyValues("catalog.synchronization.periodic." + setting)
            .run(context -> assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"run-on-startup", "full-run-on-startup"})
    void rejectsManualModes(String mode) {
        configured(mock(SynchronizeCatalog.class))
            .withPropertyValues("catalog.synchronization." + mode + "=true")
            .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void waitsForReadyAndRecoversOnNextIntervalAfterFailure() {
        var calledTwice = new CountDownLatch(2);
        var synchronize = mock(SynchronizeCatalog.class);
        when(synchronize.synchronize()).thenAnswer(invocation -> {
            calledTwice.countDown();
            if (calledTwice.getCount() == 1) {
                throw new IllegalStateException("Temporary failure");
            }
            return CatalogSynchronizationResult.UP_TO_DATE;
        });
        configured(synchronize).withPropertyValues("catalog.synchronization.periodic.interval=1s",
                "catalog.synchronization.periodic.initial-delay=0s").run(context -> {
            assertThat(context).hasNotFailed();
            verifyNoInteractions(synchronize);
            var event = new ApplicationReadyEvent(new SpringApplication(), new String[0],
                    context.getSourceApplicationContext(), Duration.ZERO);
            context.publishEvent(event);
            assertThat(calledTwice.await(5, TimeUnit.SECONDS)).isTrue();
        });
    }

    @Test
    void closingContextCancelsPendingTaskAndPreventsRestart() {
        var called = new CountDownLatch(1);
        var synchronize = mock(SynchronizeCatalog.class);
        when(synchronize.synchronize()).thenAnswer(invocation -> {
            called.countDown();
            return CatalogSynchronizationResult.UP_TO_DATE;
        });
        configured(synchronize).withPropertyValues("catalog.synchronization.periodic.initial-delay=1s")
            .run(context -> {
                var event = new ApplicationReadyEvent(new SpringApplication(), new String[0],
                        context.getSourceApplicationContext(), Duration.ZERO);
                var schedule = context.getBean(CatalogSynchronizationSchedule.class);
                context.publishEvent(event);
                context.close();
                schedule.onApplicationEvent(event);
                assertThat(called.await(1500, TimeUnit.MILLISECONDS)).isFalse();
                verifyNoInteractions(synchronize);
            });
    }

    private ApplicationContextRunner configured(SynchronizeCatalog synchronize) {
        return runner.withPropertyValues("catalog.synchronization.periodic.enabled=true")
            .withBean(SynchronizeCatalog.class, () -> synchronize);
    }
}
