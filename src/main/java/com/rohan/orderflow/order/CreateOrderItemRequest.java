package com.rohan.orderflow.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderItemRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @Positive(message = "Quantity must be greater than zero")
        int quantity

) {
}