package com.rohan.orderflow.order;

import com.rohan.orderflow.inventory.Inventory;
import com.rohan.orderflow.inventory.InventoryRepository;
import com.rohan.orderflow.outbox.OutboxEventRepository;
import com.rohan.orderflow.outbox.OutboxService;
import com.rohan.orderflow.product.Product;
import com.rohan.orderflow.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import({
        OrderService.class,
        OutboxService.class,
        OrderTransactionIntegrationTest.JacksonTestConfig.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OrderTransactionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18")
                    .withDatabaseName("orderflow_test")
                    .withUsername("orderflow")
                    .withPassword("orderflow_test");

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoSpyBean
    private OutboxService outboxService;

    private Product product;

    @BeforeEach
    void setUp() {

        outboxEventRepository.deleteAll();
        orderRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();

        product = productRepository.saveAndFlush(
                new Product(
                        "Integration Test Laptop",
                        "INT-LAPTOP-001",
                        new BigDecimal("500.00")
                )
        );

        inventoryRepository.saveAndFlush(
                new Inventory(
                        product,
                        10
                )
        );
    }

    @Test
    void shouldCommitOrderInventoryAndOutboxTogether() {

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new CreateOrderItemRequest(
                                        product.getId(),
                                        2
                                )
                        )
                );

        OrderResponse response =
                orderService.createOrder(request);

        assertThat(response.id())
                .isNotNull();

        assertThat(response.status())
                .isEqualTo(OrderStatus.PENDING);

        assertThat(response.totalAmount())
                .isEqualByComparingTo("1000.00");

        assertThat(orderRepository.count())
                .isEqualTo(1);

        assertThat(outboxEventRepository.count())
                .isEqualTo(1);

        Inventory updatedInventory =
                inventoryRepository
                        .findByProductId(product.getId())
                        .orElseThrow();

        assertThat(updatedInventory.getAvailableQuantity())
                .isEqualTo(8);

        assertThat(updatedInventory.getReservedQuantity())
                .isEqualTo(2);
    }

    @Test
    void shouldRollbackEntireTransactionWhenOutboxFails() {

        doThrow(
                new RuntimeException(
                        "Simulated outbox failure"
                )
        )
                .when(outboxService)
                .saveOrderCreated(any(Order.class));

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(
                                new CreateOrderItemRequest(
                                        product.getId(),
                                        2
                                )
                        )
                );

        assertThatThrownBy(
                () -> orderService.createOrder(request)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(
                        "Simulated outbox failure"
                );

        assertThat(orderRepository.count())
                .isZero();

        assertThat(outboxEventRepository.count())
                .isZero();

        Inventory inventoryAfterFailure =
                inventoryRepository
                        .findByProductId(product.getId())
                        .orElseThrow();

        assertThat(inventoryAfterFailure.getAvailableQuantity())
                .isEqualTo(10);

        assertThat(inventoryAfterFailure.getReservedQuantity())
                .isZero();
    }

    @TestConfiguration
    static class JacksonTestConfig {

        @Bean
        JsonMapper jsonMapper() {
            return JsonMapper.builder().build();
        }
    }
}