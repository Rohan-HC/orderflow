package com.rohan.orderflow.inventory;

public record InventoryResponse(
        Long id,
        Long productId,
        int availableQuantity,
        int reservedQuantity,
        Long version
) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getVersion()
        );
    }
}