package ar.edu.programacion2.catalog.catalog.infrastructure.startup;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class ManualFullCatalogSynchronizationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(ManualFullCatalogSynchronization.class);

    @Test
    void disabledByDefaultWithoutClients() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ManualFullCatalogSynchronization.class));
    }

    @Test
    void explicitlyEnabledModeRequiresUseCase() {
        runner.withPropertyValues("catalog.synchronization.full-run-on-startup=true", "spring.main.web-application-type=none")
            .run(context -> assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @CsvSource({"true,none", "false,servlet", "false,reactive"})
    void rejectsIncompatibleModesBeforeCallingUseCase(boolean snapshot, String webType) {
        var useCase = mock(SynchronizeCatalog.class);
        runner.withPropertyValues("catalog.synchronization.full-run-on-startup=true",
                "catalog.synchronization.run-on-startup=" + snapshot, "spring.main.web-application-type=" + webType)
            .withBean(SynchronizeCatalog.class, () -> useCase)
            .run(context -> {
                assertThat(context).hasFailed();
                verifyNoInteractions(useCase);
            });
    }

    @ParameterizedTest
    @CsvSource({"INCREMENT_APPLIED,increment applied", "SNAPSHOT_APPLIED,snapshot applied",
        "UP_TO_DATE,local version matches observed remote version", "ALREADY_APPLIED,increment already covered locally",
        "SNAPSHOT_ALREADY_COVERED,snapshot already covered locally"})
    void reportsSuccessfulResultOnce(CatalogSynchronizationResult result, String message, CapturedOutput output) {
        var useCase = mock(SynchronizeCatalog.class);
        when(useCase.synchronize()).thenReturn(result);
        runner.withPropertyValues("catalog.synchronization.full-run-on-startup=true", "spring.main.web-application-type=none")
            .withBean(SynchronizeCatalog.class, () -> useCase)
            .run(context -> {
                assertThat(context).hasNotFailed();
                verifyNoInteractions(useCase);
                context.getBean(ManualFullCatalogSynchronization.class).run(new DefaultApplicationArguments());
            });
        verify(useCase).synchronize();
        verifyNoMoreInteractions(useCase);
        assertThat(output).contains("Full catalog synchronization: " + message);
    }

    @ParameterizedTest
    @CsvSource({"RETRY_REQUIRED,remote versions changed", "LOCAL_VERSION_AHEAD,local version is ahead"})
    void unresolvedResultsFailWithoutRetry(CatalogSynchronizationResult result, String message) {
        var useCase = mock(SynchronizeCatalog.class);
        when(useCase.synchronize()).thenReturn(result);
        var command = new ManualFullCatalogSynchronization(useCase, false, "none");
        assertThatThrownBy(() -> command.run(new DefaultApplicationArguments()))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining(message).hasNoCause();
        verify(useCase).synchronize();
        verifyNoMoreInteractions(useCase);
    }

    @Test
    void sanitizesErrors(CapturedOutput output) {
        var useCase = mock(SynchronizeCatalog.class);
        when(useCase.synchronize()).thenThrow(new IllegalStateException("sensitive-fixture-value"));
        var command = new ManualFullCatalogSynchronization(useCase, false, "none");
        assertThatThrownBy(() -> command.run(new DefaultApplicationArguments()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Full catalog synchronization failed; review connectivity and configuration").hasNoCause();
        verify(useCase).synchronize();
        verifyNoMoreInteractions(useCase);
        assertThat(output).doesNotContain("sensitive-fixture-value");
    }
}
