package com.rohan.orderflow.outbox;

import com.rohan.orderflow.config.KafkaConfig;
import com.rohan.orderflow.event.OrderCreatedEvent;
import com.rohan.orderflow.order.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;

import java.time.Instant;

@Service
public class OutboxService {

    private final OutboxEventRepository repository;
    private final JsonMapper jsonMapper;

    public OutboxService(
            OutboxEventRepository repository,
            JsonMapper jsonMapper
    ) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void saveOrderCreated(Order order) 
    {
        UUID eventId = UUID.randomUUID();
       OrderCreatedEvent event =
        new OrderCreatedEvent(
                eventId,
                order.getId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                Instant.now()
        );

        try {

            String payload =
                    jsonMapper.writeValueAsString(event);

           OutboxEvent outboxEvent =
        new OutboxEvent(
                eventId,
                "ORDER",
                order.getId().toString(),
                "ORDER_CREATED",
                KafkaConfig.ORDER_CREATED_TOPIC,
                order.getId().toString(),
                payload
        );

            repository.save(outboxEvent);

        } catch (JacksonException exception) {

            throw new IllegalStateException(
                    "Failed to serialize order created event",
                    exception
            );
        }
    }
}