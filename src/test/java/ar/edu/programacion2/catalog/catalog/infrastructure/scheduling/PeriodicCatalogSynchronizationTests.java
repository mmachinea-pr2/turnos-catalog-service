package ar.edu.programacion2.catalog.catalog.infrastructure.scheduling;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class PeriodicCatalogSynchronizationTests {
    @ParameterizedTest
    @EnumSource(CatalogSynchronizationResult.class)
    void eachInvocationPerformsOnlyOneCheckAndAllowsTheNext(CatalogSynchronizationResult result) {
        var synchronize = mock(SynchronizeCatalog.class);
        when(synchronize.synchronize()).thenReturn(result);
        var task = new PeriodicCatalogSynchronization(synchronize);

        task.run();
        verify(synchronize).synchronize();
        task.run();
        verify(synchronize, times(2)).synchronize();
    }

    @Test
    void failureDoesNotLeakCauseOrPreventLaterRecovery(CapturedOutput output) {
        var synchronize = mock(SynchronizeCatalog.class);
        when(synchronize.synchronize()).thenThrow(new IllegalStateException("SECRET_REMOTE_RESPONSE"))
            .thenReturn(CatalogSynchronizationResult.SNAPSHOT_APPLIED);
        var task = new PeriodicCatalogSynchronization(synchronize);

        assertThatCode(task::run).doesNotThrowAnyException();
        task.run();

        verify(synchronize, times(2)).synchronize();
        assertThat(output.getAll()).contains("synchronization failed", "SNAPSHOT_APPLIED")
            .doesNotContain("SECRET_REMOTE_RESPONSE");
    }

    @Test
    void unexpectedNullDoesNotPreventTheNextCheck() {
        var synchronize = mock(SynchronizeCatalog.class);
        when(synchronize.synchronize()).thenReturn(null, CatalogSynchronizationResult.UP_TO_DATE);
        var task = new PeriodicCatalogSynchronization(synchronize);
        assertThatCode(task::run).doesNotThrowAnyException();
        task.run();
        verify(synchronize, times(2)).synchronize();
    }

    @Test
    void overlappingInvocationIsSkippedAndLaterInvocationCanRun() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var synchronize = mock(SynchronizeCatalog.class);
        when(synchronize.synchronize()).thenAnswer(invocation -> {
            entered.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Test timed out");
            }
            return CatalogSynchronizationResult.UP_TO_DATE;
        });
        var task = new PeriodicCatalogSynchronization(synchronize);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var first = executor.submit(task);
            try {
                assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
                task.run();
                verify(synchronize).synchronize();
            } finally {
                release.countDown();
            }
            first.get(5, TimeUnit.SECONDS);
            task.run();
            verify(synchronize, times(2)).synchronize();
        }
    }
}
