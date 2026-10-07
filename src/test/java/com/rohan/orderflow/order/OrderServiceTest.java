package com.rohan.orderflow.order;

import com.rohan.orderflow.inventory.Inventory;
import com.rohan.orderflow.inventory.InventoryRepository;
import com.rohan.orderflow.inventory.InsufficientInventoryException;
import com.rohan.orderflow.outbox.OutboxService;
import com.rohan.orderflow.product.Product;
import com.rohan.orderflow.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OutboxService outboxService;

    private OrderService orderService;

    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {

        orderService = new OrderService(
                orderRepository,
                productRepository,
                inventoryRepository,
                outboxService
        );

        product = new Product(
                "Gaming Laptop",
                "LAPTOP-001",
                new BigDecimal("899.99")
        );

        ReflectionTestUtils.setField(
                product,
                "id",
                1L
        );

        inventory = new Inventory(
                product,
                10
        );
    }

    @Test
    void shouldCreateOrderAndReserveInventory() {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new CreateOrderItemRequest(
                                        1L,
                                        2
                                )
                        )
                );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        OrderResponse response =
                orderService.createOrder(request);

        assertThat(response.status())
                .isEqualTo(OrderStatus.PENDING);

        assertThat(response.totalAmount())
                .isEqualByComparingTo("1799.98");

        assertThat(response.items())
                .hasSize(1);

        assertThat(inventory.getAvailableQuantity())
                .isEqualTo(8);

        assertThat(inventory.getReservedQuantity())
                .isEqualTo(2);

        verify(orderRepository)
                .save(any(Order.class));

        verify(outboxService)
                .saveOrderCreated(any(Order.class));
    }

    @Test
    void shouldRejectOrderWhenInventoryIsInsufficient() {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new CreateOrderItemRequest(
                                        1L,
                                        100
                                )
                        )
                );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        assertThatThrownBy(
                () -> orderService.createOrder(request)
        )
                .isInstanceOf(
                        InsufficientInventoryException.class
                );

        assertThat(inventory.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(inventory.getReservedQuantity())
                .isZero();

        verify(orderRepository, never())
                .save(any(Order.class));

        verify(outboxService, never())
                .saveOrderCreated(any(Order.class));
    }

    @Test
    void shouldConfirmPendingOrder() {

        Order order = new Order();

        ReflectionTestUtils.setField(
                order,
                "id",
                1L
        );

        order.addItem(
                product,
                2
        );

        inventory.reserve(2);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        OrderResponse response =
                orderService.confirmOrder(1L);

        assertThat(response.status())
                .isEqualTo(OrderStatus.CONFIRMED);

        assertThat(inventory.getAvailableQuantity())
                .isEqualTo(8);

        assertThat(inventory.getReservedQuantity())
                .isZero();
    }

    @Test
    void shouldCancelPendingOrderAndReleaseInventory() {

        Order order = new Order();

        ReflectionTestUtils.setField(
                order,
                "id",
                1L
        );

        order.addItem(
                product,
                2
        );

        inventory.reserve(2);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        OrderResponse response =
                orderService.cancelOrder(1L);

        assertThat(response.status())
                .isEqualTo(OrderStatus.CANCELLED);

        assertThat(inventory.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(inventory.getReservedQuantity())
                .isZero();
    }

    @Test
    void shouldRejectSecondConfirmation() {

        Order order = new Order();

        ReflectionTestUtils.setField(
                order,
                "id",
                1L
        );

        order.addItem(
                product,
                2
        );

        inventory.reserve(2);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(inventoryRepository.findByProductId(1L))
                .thenReturn(Optional.of(inventory));

        orderService.confirmOrder(1L);

        assertThatThrownBy(
                () -> orderService.confirmOrder(1L)
        )
                .isInstanceOf(
                        InvalidOrderStateException.class
                );
    }
}