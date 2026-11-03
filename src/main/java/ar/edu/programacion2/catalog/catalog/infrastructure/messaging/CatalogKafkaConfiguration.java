package ar.edu.programacion2.catalog.catalog.infrastructure.messaging;

import ar.edu.programacion2.catalog.catalog.application.port.in.HandleCatalogUpdated;
import java.util.HashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonContainerStoppingErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "catalog.catedra.kafka.enabled", havingValue = "true")
public class CatalogKafkaConfiguration {
    @Bean
    KafkaMessageListenerContainer<String, String> catalogKafkaContainer(HandleCatalogUpdated handler,
            @Value("${catalog.catedra.kafka.bootstrap-servers:}") String brokers,
            @Value("${catalog.catedra.kafka.topic:}") String topic,
            @Value("${catalog.catedra.kafka.group-id:}") String group,
            @Value("${catalog.catedra.kafka.auto-startup:true}") boolean autoStartup,
            @Value("${catalog.synchronization.run-on-startup:false}") boolean manualSnapshot,
            @Value("${catalog.synchronization.full-run-on-startup:false}") boolean manualFull) {
        if (brokers.isBlank() || group.isBlank() || topic.isBlank() || topic.length() > 249
                || !topic.matches("[a-zA-Z0-9._-]+") || topic.equals(".") || topic.equals("..")) {
            throw new IllegalArgumentException("Kafka brokers, group and a valid topic are required");
        }
        if (manualSnapshot || manualFull) {
            throw new IllegalArgumentException("Kafka consumption cannot run in a manual synchronization command");
        }
        var config = new HashMap<String, Object>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, false);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        var properties = new ContainerProperties(topic);
        properties.setAckMode(ContainerProperties.AckMode.RECORD);
        properties.setSyncCommits(true);
        properties.setMissingTopicsFatal(true);
        properties.setMessageListener(new CatalogKafkaListener(handler));
        var container = new KafkaMessageListenerContainer<>(
            new DefaultKafkaConsumerFactory<String, String>(config), properties);
        container.setCommonErrorHandler(new CommonContainerStoppingErrorHandler());
        container.setAutoStartup(autoStartup);
        return container;
    }

    @Bean
    HealthIndicator catalogKafkaHealthIndicator(KafkaMessageListenerContainer<String, String> catalogKafkaContainer) {
        return () -> {
            var assignments = catalogKafkaContainer.getAssignedPartitions();
            boolean active = catalogKafkaContainer.isRunning() && assignments != null && !assignments.isEmpty();
            return (active ? Health.up() : Health.down()).build();
        };
    }
}
