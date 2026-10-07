package com.rohan.orderflow.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Long orderId,
        String status,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}