package ar.edu.programacion2.catalog.catalog.infrastructure.messaging;

import ar.edu.programacion2.catalog.catalog.application.port.in.HandleCatalogUpdated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogKafkaConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(CatalogKafkaConfiguration.class);

    @Test
    void disabledNeedsNoClients() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(KafkaMessageListenerContainer.class));
    }

    @Test
    void missingHandlerFailsRatherThanSilentlySkippingConsumption() {
        configured().run(context -> assertThat(context).hasFailed());
    }

    @Test
    void enabledContainerUsesRecordAcknowledgmentAndReportsStoppedState() {
        var handler = mock(HandleCatalogUpdated.class);
        configured().withBean(HandleCatalogUpdated.class, () -> handler).run(context -> {
            assertThat(context).hasNotFailed();
            var container = context.getBean(KafkaMessageListenerContainer.class);
            assertThat(container.isAutoStartup()).isFalse();
            assertThat(container.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.RECORD);
            assertThat(context.getBean(HealthIndicator.class).health().getStatus().getCode()).isEqualTo("DOWN");
            verifyNoInteractions(handler);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"catalog.catedra.kafka.bootstrap-servers=", "catalog.catedra.kafka.group-id=",
        "catalog.catedra.kafka.topic=", "catalog.catedra.kafka.topic=bad topic", "catalog.catedra.kafka.topic=..",
        "catalog.synchronization.run-on-startup=true", "catalog.synchronization.full-run-on-startup=true"})
    void rejectsInvalidOrConflictingConfiguration(String property) {
        var handler = mock(HandleCatalogUpdated.class);
        configured().withPropertyValues(property).withBean(HandleCatalogUpdated.class, () -> handler)
            .run(context -> {
                assertThat(context).hasFailed();
                verifyNoInteractions(handler);
            });
    }

    private ApplicationContextRunner configured() {
        return runner.withPropertyValues("catalog.catedra.kafka.enabled=true", "catalog.catedra.kafka.auto-startup=false",
            "catalog.catedra.kafka.bootstrap-servers=127.0.0.1:1", "catalog.catedra.kafka.topic=catedra.catalog.test",
            "catalog.catedra.kafka.group-id=test-group");
    }
}
