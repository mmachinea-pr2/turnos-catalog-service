package ar.edu.programacion2.catalog.catalog.infrastructure.startup;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import ar.edu.programacion2.catalog.catalog.application.port.out.SnapshotFetchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class ManualCatalogSynchronizationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(ManualCatalogSynchronization.class);

    @Test
    void regularStartupDoesNotRequireSynchronization() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ManualCatalogSynchronization.class));
    }

    @Test
    void explicitExecutionRequiresConfiguredUseCase() {
        runner.withPropertyValues("catalog.synchronization.run-on-startup=true")
            .run(context -> assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void logsResultAndInvokesUseCaseExactlyOnce(boolean applied, CapturedOutput output) {
        var useCase = mock(SynchronizeCatalogSnapshot.class);
        when(useCase.synchronize()).thenReturn(applied);
        runner.withPropertyValues("catalog.synchronization.run-on-startup=true")
            .withBean(SynchronizeCatalogSnapshot.class, () -> useCase)
            .run(context -> {
                context.getBean(ManualCatalogSynchronization.class).run(new DefaultApplicationArguments());
                verify(useCase).synchronize();
                verifyNoMoreInteractions(useCase);
            });
        assertThat(output).contains(applied ? "snapshot applied" : "version already covered, no changes");
    }

    @Test
    void reportsSafeRestErrorWithoutExposingCause() {
        var command = new ManualCatalogSynchronization(() -> {
            throw new SnapshotFetchException("Snapshot request failed: HTTP 503");
        });
        assertThatThrownBy(() -> command.run(new DefaultApplicationArguments()))
            .hasMessage("Catalog synchronization failed: Snapshot request failed: HTTP 503").hasNoCause();
    }

    @Test
    void failureStopsExecutionWithoutLeakingCauseOrRetrying(CapturedOutput output) {
        var useCase = mock(SynchronizeCatalogSnapshot.class);
        when(useCase.synchronize()).thenThrow(new IllegalStateException("sensitive-fixture-value"));
        var command = new ManualCatalogSynchronization(useCase);
        assertThatThrownBy(() -> command.run(new DefaultApplicationArguments()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Catalog synchronization failed; no new snapshot was committed").hasNoCause();
        verify(useCase).synchronize();
        verifyNoMoreInteractions(useCase);
        assertThat(output).doesNotContain("sensitive-fixture-value").doesNotContain("snapshot applied");
    }
}
