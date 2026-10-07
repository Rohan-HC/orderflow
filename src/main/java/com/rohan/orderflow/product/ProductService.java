package com.rohan.orderflow.product;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

public Product createProduct(CreateProductRequest request) {

    if (productRepository.existsBySku(request.sku())) {
        throw new DuplicateSkuException(request.sku());
    }

    Product product = new Product(
            request.name(),
            request.sku(),
            request.price()
    );

    return productRepository.save(product);
}

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}