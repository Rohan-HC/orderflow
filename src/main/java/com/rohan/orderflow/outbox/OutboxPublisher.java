package com.rohan.orderflow.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                repository
                        .findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {

            try {

                kafkaTemplate
                        .send(
                                event.getTopic(),
                                event.getEventKey(),
                                event.getPayload()
                        )
                        .get(10, TimeUnit.SECONDS);

                event.markPublished();

            } catch (Exception exception) {

                event.markFailed(
                        exception.getMessage()
                );
            }
        }
    }
}