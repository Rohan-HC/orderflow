package com.rohan.orderflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class KafkaConfig {

    public static final String ORDER_CREATED_TOPIC =
            "order.created.v1";

    @Bean
    public NewTopic orderCreatedTopic() {

        return TopicBuilder
                .name(ORDER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}