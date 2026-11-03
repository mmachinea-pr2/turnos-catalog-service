package ar.edu.programacion2.catalog.catalog.infrastructure.messaging;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogEventResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.HandleCatalogUpdated;
import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@Testcontainers
class CatalogKafkaIntegrationTests {
    @Container
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka-native:3.8.0");

    @Test
    void commitsSuccessAndDuplicatesButRedeliversFailureAfterRestart() throws Exception {
        String topic = "catalog-" + UUID.randomUUID();
        String group = "group-" + UUID.randomUUID();
        var fail = new AtomicBoolean(true);
        var sevenCalls = new AtomicInteger();
        var eightCalls = new AtomicInteger();
        HandleCatalogUpdated handler = event -> {
            if (event.newVersion() == 8) {
                eightCalls.incrementAndGet();
                return fail.get() ? CatalogEventResult.RETRY_REQUIRED : CatalogEventResult.PROCESSED;
            }
            return sevenCalls.incrementAndGet() == 1 ? CatalogEventResult.PROCESSED : CatalogEventResult.DUPLICATE;
        };
        try (var admin = AdminClient.create(Map.of("bootstrap.servers", KAFKA.getBootstrapServers()));
                var producer = producer()) {
            admin.createTopics(List.of(new NewTopic(topic, 1, (short) 1))).all().get(20, TimeUnit.SECONDS);
            send(producer, topic, "7", CatalogKafkaListenerTests.JSON);
            send(producer, topic, "8", CatalogKafkaListenerTests.JSON.replace("event-7", "event-8").replace("\"newVersion\":7", "\"newVersion\":8"));
            context(topic, group, handler).run(context -> {
                assertThat(context).hasNotFailed();
                var container = context.getBean(KafkaMessageListenerContainer.class);
                await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> {
                    assertThat(eightCalls.get()).isEqualTo(1);
                    assertThat(container.isRunning()).isFalse();
                    assertThat(offset(admin, group, topic)).isEqualTo(1);
                });
                assertThat(context.getBean(HealthIndicator.class).health().getStatus().getCode()).isEqualTo("DOWN");
                fail.set(false);
                container.start();
                await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> assertThat(offset(admin, group, topic)).isEqualTo(2));
                assertThat(eightCalls.get()).isEqualTo(2);
                assertThat(sevenCalls.get()).isEqualTo(1);
            });
            // Nuevo contexto y mismo grupo: retoma el offset confirmado, no desde el inicio.
            send(producer, topic, "7", CatalogKafkaListenerTests.JSON);
            context(topic, group, handler).run(context -> {
                assertThat(context).hasNotFailed();
                await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> assertThat(offset(admin, group, topic)).isEqualTo(3));
                assertThat(sevenCalls.get()).isEqualTo(2);
                assertThat(eightCalls.get()).isEqualTo(2);
                assertThat(context.getBean(HealthIndicator.class).health().getStatus().getCode()).isEqualTo("UP");
            });
        }
    }

    @Test
    void malformedEventStopsWithoutCommittingOrCallingApplication() throws Exception {
        String topic = "catalog-invalid-" + UUID.randomUUID();
        String group = "group-" + UUID.randomUUID();
        var calls = new AtomicInteger();
        try (var admin = AdminClient.create(Map.of("bootstrap.servers", KAFKA.getBootstrapServers()));
                var producer = producer()) {
            admin.createTopics(List.of(new NewTopic(topic, 1, (short) 1))).all().get(20, TimeUnit.SECONDS);
            send(producer, topic, "7", "invalid-json");
            context(topic, group, event -> { calls.incrementAndGet(); return CatalogEventResult.PROCESSED; }).run(context -> {
                assertThat(context).hasNotFailed();
                var container = context.getBean(KafkaMessageListenerContainer.class);
                await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> assertThat(container.isRunning()).isFalse());
                assertThat(offset(admin, group, topic)).isLessThanOrEqualTo(0);
                assertThat(calls.get()).isZero();
                assertThat(context.getBean(HealthIndicator.class).health().getStatus().getCode()).isEqualTo("DOWN");
            });
        }
    }

    private ApplicationContextRunner context(String topic, String group, HandleCatalogUpdated handler) {
        return new ApplicationContextRunner().withUserConfiguration(CatalogKafkaConfiguration.class)
            .withBean(HandleCatalogUpdated.class, () -> handler)
            .withPropertyValues("catalog.catedra.kafka.enabled=true",
                "catalog.catedra.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers(),
                "catalog.catedra.kafka.topic=" + topic, "catalog.catedra.kafka.group-id=" + group);
    }

    private KafkaProducer<String, String> producer() {
        return new KafkaProducer<>(Map.of("bootstrap.servers", KAFKA.getBootstrapServers(), "acks", "all"),
            new StringSerializer(), new StringSerializer());
    }

    private void send(KafkaProducer<String, String> producer, String topic, String key, String value) throws Exception {
        producer.send(new ProducerRecord<>(topic, key, value)).get(20, TimeUnit.SECONDS);
    }

    private long offset(AdminClient admin, String group, String topic) {
        try {
            var offsets = admin.listConsumerGroupOffsets(group).partitionsToOffsetAndMetadata().get(10, TimeUnit.SECONDS);
            var offset = offsets.get(new TopicPartition(topic, 0));
            return offset == null ? -1 : offset.offset();
        } catch (Exception exception) {
            throw new AssertionError("Could not read test consumer offset", exception);
        }
    }
}
