package com.rohan.orderflow.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        String status,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}