package com.rohan.orderflow.consumer;

import com.rohan.orderflow.config.KafkaConfig;
import com.rohan.orderflow.event.OrderCreatedEvent;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OrderCreatedConsumer {

    private final JsonMapper jsonMapper;
    private final ProcessedEventRepository processedEventRepository;

    public OrderCreatedConsumer(
            JsonMapper jsonMapper,
            ProcessedEventRepository processedEventRepository
    ) {
        this.jsonMapper = jsonMapper;
        this.processedEventRepository = processedEventRepository;
    }

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 5000
            ),
            retryTopicSuffix = "-retry",
            dltTopicSuffix = "-dlt",
            autoCreateTopics = "true",
            numPartitions = "3",
            replicationFactor = "1"
    )
    @KafkaListener(
            topics = KafkaConfig.ORDER_CREATED_TOPIC,
            groupId = "orderflow-order-created-consumer"
    )
    @Transactional
    public void consume(String payload) {

        OrderCreatedEvent event;

        try {
            event = jsonMapper.readValue(
                    payload,
                    OrderCreatedEvent.class
            );
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "Invalid ORDER_CREATED payload",
                    exception
            );
        }

        if (event.eventId() == null) {
            throw new IllegalArgumentException(
                    "ORDER_CREATED eventId is required"
            );
        }

        if (processedEventRepository.existsById(event.eventId())) {

            System.out.println(
                    "Skipping duplicate event: "
                            + event.eventId()
            );

            return;
        }

        System.out.println(
                "Processing ORDER_CREATED event "
                        + event.eventId()
                        + " for order "
                        + event.orderId()
        );

        processedEventRepository.save(
                new ProcessedEvent(
                        event.eventId(),
                        "ORDER_CREATED"
                )
        );
    }

    @DltHandler
    public void handleDlt(String payload) {

        System.err.println(
                "ORDER_CREATED moved to DLT: "
                        + payload
        );
    }
}