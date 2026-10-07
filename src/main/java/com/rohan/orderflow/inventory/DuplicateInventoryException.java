package com.rohan.orderflow.inventory;

public class DuplicateInventoryException extends RuntimeException {

    public DuplicateInventoryException(Long productId) {
        super("Inventory already exists for product id: " + productId);
    }
}