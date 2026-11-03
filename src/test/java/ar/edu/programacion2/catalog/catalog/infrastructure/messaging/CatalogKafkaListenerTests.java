package ar.edu.programacion2.catalog.catalog.infrastructure.messaging;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogEventResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.HandleCatalogUpdated;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogKafkaListenerTests {
    private final HandleCatalogUpdated handler = mock(HandleCatalogUpdated.class);
    private final CatalogKafkaListener listener = new CatalogKafkaListener(handler);
    static final String JSON = """
        {"eventId":"event-7","eventType":"CatalogUpdated","newVersion":7,
        "occurredAt":"2026-09-10T13:44:59Z","source":"MANUAL","schemaVersion":1}
        """;

    @ParameterizedTest
    @EnumSource(value = CatalogEventResult.class, names = {"PROCESSED", "DUPLICATE"})
    void returnsOnlyForProcessedOrDuplicate(CatalogEventResult result) {
        when(handler.handle(any())).thenReturn(result);
        listener.onMessage(new ConsumerRecord<>("catalog", 0, 0L, "7", JSON));
        verify(handler).handle(argThat(event -> event.newVersion() == 7 && event.eventId().equals("event-7")));
    }

    @ParameterizedTest
    @EnumSource(value = CatalogEventResult.class, names = {"RETRY_REQUIRED", "LOCAL_VERSION_AHEAD"})
    void unresolvedEventsMustNotBeAcknowledged(CatalogEventResult result) {
        when(handler.handle(any())).thenReturn(result);
        assertThatThrownBy(() -> listener.onMessage(new ConsumerRecord<>("catalog", 0, 0L, "7", JSON)))
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("remains pending").hasNoCause();
    }

    @Test
    void malformedMessageNeverReachesHandler() {
        assertThatThrownBy(() -> listener.onMessage(new ConsumerRecord<>("catalog", 0, 0L, "7", "invalid-payload")))
            .hasMessage("Catalog Kafka event failed; offset must not be committed").hasNoCause();
        verifyNoInteractions(handler);
    }

    @Test
    void masksInternalFailure() {
        when(handler.handle(any())).thenThrow(new IllegalStateException("sensitive-fixture-value"));
        assertThatThrownBy(() -> listener.onMessage(new ConsumerRecord<>("catalog", 0, 0L, "7", JSON)))
            .hasMessage("Catalog Kafka event failed; offset must not be committed").hasNoCause();
    }
}
