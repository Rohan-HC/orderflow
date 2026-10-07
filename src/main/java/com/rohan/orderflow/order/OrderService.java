package com.rohan.orderflow.order;

import com.rohan.orderflow.inventory.Inventory;
import com.rohan.orderflow.inventory.InventoryNotFoundException;
import com.rohan.orderflow.inventory.InventoryRepository;
import com.rohan.orderflow.product.Product;
import com.rohan.orderflow.product.ProductNotFoundException;
import com.rohan.orderflow.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Order order = new Order();

        for (CreateOrderItemRequest itemRequest : request.items()) {

            Product product = productRepository
                    .findById(itemRequest.productId())
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    itemRequest.productId()
                            )
                    );

            Inventory inventory = inventoryRepository
                    .findByProductId(itemRequest.productId())
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    itemRequest.productId()
                            )
                    );

            inventory.reserve(itemRequest.quantity());

            order.addItem(
                    product,
                    itemRequest.quantity()
            );
        }

        Order savedOrder = orderRepository.save(order);

        return OrderResponse.from(savedOrder);
    }
}