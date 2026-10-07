package com.rohan.orderflow.consumer;

import com.rohan.orderflow.config.KafkaConfig;
import com.rohan.orderflow.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.secret=OrderFlow-Integration-Test-Secret-Key-2026"
})
class OrderCreatedKafkaIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18")
                    .withDatabaseName("orderflow_test")
                    .withUsername("orderflow")
                    .withPassword("orderflow_test");

    @Container
    @ServiceConnection
    static KafkaContainer kafka =
            new KafkaContainer("apache/kafka:4.1.2");

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void shouldConsumeOrderCreatedEventAndRecordItAsProcessed()
            throws Exception {

        UUID eventId = UUID.randomUUID();

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        eventId,
                        1001L,
                        "PENDING",
                        new BigDecimal("999.99"),
                        Instant.now()
                );

        String payload =
                jsonMapper.writeValueAsString(event);

        kafkaTemplate.send(
                        KafkaConfig.ORDER_CREATED_TOPIC,
                        "1001",
                        payload
                )
                .get(10, TimeUnit.SECONDS);

        boolean processed = false;

        for (int i = 0; i < 30; i++) {

            if (processedEventRepository.existsById(eventId)) {
                processed = true;
                break;
            }

            Thread.sleep(500);
        }

        assertThat(processed)
                .as("Kafka event should be consumed and recorded")
                .isTrue();

        ProcessedEvent stored =
                processedEventRepository
                        .findById(eventId)
                        .orElseThrow();

        assertThat(stored.getEventId())
                .isEqualTo(eventId);
    }
}