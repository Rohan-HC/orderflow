package com.rohan.orderflow.order;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(
            Long orderId,
            OrderStatus status,
            String operation
    ) {
        super(
                "Cannot " + operation +
                " order " + orderId +
                " while status is " + status
        );
    }
}