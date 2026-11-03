package ar.edu.programacion2.catalog.catalog.infrastructure.messaging;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogEventResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.HandleCatalogUpdated;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

public class CatalogKafkaListener implements MessageListener<String, String> {
    private final CatalogUpdatedEventDecoder decoder = new CatalogUpdatedEventDecoder();
    private final HandleCatalogUpdated handler;

    public CatalogKafkaListener(HandleCatalogUpdated handler) {
        this.handler = handler;
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> record) {
        final CatalogEventResult result;
        try {
            result = handler.handle(decoder.decode(record.key(), record.value()));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Catalog Kafka event failed; offset must not be committed");
        }
        if (result != CatalogEventResult.PROCESSED && result != CatalogEventResult.DUPLICATE) {
            throw new IllegalStateException("Catalog Kafka event remains pending; offset must not be committed");
        }
    }
}
