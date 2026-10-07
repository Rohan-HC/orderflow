package com.rohan.orderflow.inventory;

import jakarta.validation.constraints.Positive;

public record StockQuantityRequest(

        @Positive(message = "Quantity must be greater than zero")
        int quantity

) {
}