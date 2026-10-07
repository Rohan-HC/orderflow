package com.rohan.orderflow.order;

import java.util.List;
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
    @Transactional(readOnly = true)
public List<OrderResponse> getAllOrders() {

    return orderRepository.findAll()
            .stream()
            .map(OrderResponse::from)
            .toList();
}
@Transactional(readOnly = true)
public OrderResponse getOrderById(Long id) {

    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

    return OrderResponse.from(order);
}
@Transactional
public OrderResponse confirmOrder(Long id) {

    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

    for (OrderItem item : order.getItems()) {

        Long productId = item.getProduct().getId();

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        inventory.confirmReservation(item.getQuantity());
    }

    order.confirm();

    return OrderResponse.from(order);
}
@Transactional
public OrderResponse cancelOrder(Long id) {

    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

    for (OrderItem item : order.getItems()) {

        Long productId = item.getProduct().getId();

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        inventory.releaseReservation(item.getQuantity());
    }

    order.cancel();

    return OrderResponse.from(order);
}
}