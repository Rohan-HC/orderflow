package com.rohan.orderflow.inventory;

import com.rohan.orderflow.product.Product;
import com.rohan.orderflow.product.ProductNotFoundException;
import com.rohan.orderflow.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryResponse createInventory(
            CreateInventoryRequest request
    ) {

        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new DuplicateInventoryException(request.productId());
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() ->
                        new ProductNotFoundException(request.productId())
                );

        Inventory inventory = new Inventory(
                product,
                request.availableQuantity()
        );

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        return InventoryResponse.from(savedInventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        return InventoryResponse.from(inventory);
    }
}