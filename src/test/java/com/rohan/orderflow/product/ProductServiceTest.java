package com.rohan.orderflow.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void shouldReturnProductWhenProductExists() {

        Product product = new Product(
                "Gaming Laptop",
                "LAPTOP-001",
                new BigDecimal("899.99")
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result =
                productService.getProductById(1L);

        assertThat(result.getName())
                .isEqualTo("Gaming Laptop");

        assertThat(result.getSku())
                .isEqualTo("LAPTOP-001");

        assertThat(result.getPrice())
                .isEqualByComparingTo("899.99");

        verify(productRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> productService.getProductById(999L)
        )
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository)
                .findById(999L);
    }

    @Test
    void shouldCreateProduct() {

        CreateProductRequest request =
                new CreateProductRequest(
                        "Mechanical Keyboard",
                        "KEYBOARD-001",
                        new BigDecimal("89.99")
                );

        when(productRepository.existsBySku("KEYBOARD-001"))
                .thenReturn(false);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Product result =
                productService.createProduct(request);

        assertThat(result.getName())
                .isEqualTo("Mechanical Keyboard");

        assertThat(result.getSku())
                .isEqualTo("KEYBOARD-001");

        assertThat(result.getPrice())
                .isEqualByComparingTo("89.99");

        verify(productRepository)
                .existsBySku("KEYBOARD-001");

        verify(productRepository)
                .save(any(Product.class));
    }

    @Test
    void shouldRejectDuplicateSkuWhenCreatingProduct() {

        CreateProductRequest request =
                new CreateProductRequest(
                        "Another Laptop",
                        "LAPTOP-001",
                        new BigDecimal("999.99")
                );

        when(productRepository.existsBySku("LAPTOP-001"))
                .thenReturn(true);

        assertThatThrownBy(
                () -> productService.createProduct(request)
        )
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("LAPTOP-001");

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void shouldUpdateExistingProduct() {

        Product product = new Product(
                "Gaming Laptop",
                "LAPTOP-001",
                new BigDecimal("899.99")
        );

        UpdateProductRequest request =
                new UpdateProductRequest(
                        "Gaming Laptop Pro",
                        "LAPTOP-001",
                        new BigDecimal("949.99")
                );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(
                productRepository.existsBySkuAndIdNot(
                        "LAPTOP-001",
                        1L
                )
        ).thenReturn(false);

        Product result =
                productService.updateProduct(
                        1L,
                        request
                );

        assertThat(result.getName())
                .isEqualTo("Gaming Laptop Pro");

        assertThat(result.getPrice())
                .isEqualByComparingTo("949.99");

        verify(productRepository)
                .findById(1L);

        verify(productRepository)
                .existsBySkuAndIdNot(
                        "LAPTOP-001",
                        1L
                );
    }

    @Test
    void shouldDeleteExistingProduct() {

        Product product = new Product(
                "Gaming Laptop",
                "LAPTOP-001",
                new BigDecimal("899.99")
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository)
                .findById(1L);

        verify(productRepository)
                .delete(product);
    }
}