package com.rohan.orderflow.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody CreateInventoryRequest request
    ) {

        InventoryResponse response =
                inventoryService.createInventory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{productId}")
    public InventoryResponse getInventory(
            @PathVariable Long productId
    ) {
        return inventoryService
                .getInventoryByProductId(productId);
    }

    @PostMapping("/{productId}/reserve")
public InventoryResponse reserveStock(
        @PathVariable Long productId,
        @Valid @RequestBody StockQuantityRequest request
) {

    return inventoryService.reserveStock(
            productId,
            request
    );
}

    @PostMapping("/{productId}/stock")
public InventoryResponse addStock(
        @PathVariable Long productId,
        @Valid @RequestBody StockQuantityRequest request
) {

    return inventoryService.addStock(
            productId,
            request
    );
}
}