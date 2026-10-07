package com.rohan.orderflow.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ProductRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18")
                    .withDatabaseName("orderflow_test")
                    .withUsername("orderflow")
                    .withPassword("orderflow_test");

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldPersistAndLoadProduct() {

        Product product = new Product(
                "Test Laptop",
                "TEST-LAPTOP-001",
                new BigDecimal("799.99")
        );

        Product saved =
                productRepository.save(product);

        Optional<Product> found =
                productRepository.findById(saved.getId());

        assertThat(found)
                .isPresent();

        assertThat(found.get().getName())
                .isEqualTo("Test Laptop");

        assertThat(found.get().getSku())
                .isEqualTo("TEST-LAPTOP-001");

        assertThat(found.get().getPrice())
                .isEqualByComparingTo("799.99");
    }

    @Test
    void shouldDetectExistingSku() {

        productRepository.save(
                new Product(
                        "Keyboard",
                        "TEST-KEYBOARD-001",
                        new BigDecimal("99.99")
                )
        );

        boolean exists =
                productRepository
                        .existsBySku(
                                "TEST-KEYBOARD-001"
                        );

        assertThat(exists)
                .isTrue();
    }

    @Test
    void shouldEnforceUniqueSku() {

        productRepository.saveAndFlush(
                new Product(
                        "Laptop One",
                        "DUPLICATE-001",
                        new BigDecimal("500.00")
                )
        );

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() ->
                        productRepository.saveAndFlush(
                                new Product(
                                        "Laptop Two",
                                        "DUPLICATE-001",
                                        new BigDecimal("600.00")
                                )
                        )
                )
                .isInstanceOf(
                        org.springframework.dao.DataIntegrityViolationException.class
                );
    }
}