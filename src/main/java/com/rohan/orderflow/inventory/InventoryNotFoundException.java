package com.rohan.orderflow.inventory;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(Long productId) {
        super("Inventory not found for product id: " + productId);
    }
}