package com.rohan.orderflow.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
public ResponseEntity<Product> createProduct(
        @Valid @RequestBody CreateProductRequest request) {

    Product createdProduct = productService.createProduct(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdProduct);
}

    @PutMapping("/{id}")
public Product updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody UpdateProductRequest request
) {
    return productService.updateProduct(id, request);
}

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
}