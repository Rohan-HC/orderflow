package com.rohan.orderflow.order;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {

        OrderResponse response =
                orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
public List<OrderResponse> getAllOrders() {
    return orderService.getAllOrders();
}
@GetMapping("/{id}")
public OrderResponse getOrderById(
        @PathVariable Long id
) {
    return orderService.getOrderById(id);
}
@PostMapping("/{id}/confirm")
public OrderResponse confirmOrder(
        @PathVariable Long id
) {
    return orderService.confirmOrder(id);
}
@PostMapping("/{id}/cancel")
public OrderResponse cancelOrder(
        @PathVariable Long id
) {
    return orderService.cancelOrder(id);
}
}