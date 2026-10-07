package com.rohan.orderflow.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateInventoryRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @PositiveOrZero(message = "Available quantity cannot be negative")
        int availableQuantity

) {
}