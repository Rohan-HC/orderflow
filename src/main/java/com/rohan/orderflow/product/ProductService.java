package com.rohan.orderflow.product;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

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
    @Cacheable(
        cacheNames = "products",
        key = "#id"
)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
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

    @CacheEvict(
        cacheNames = "products",
        key = "#id"
)
    @Transactional
    public Product updateProduct(
            Long id,
            UpdateProductRequest request
    ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new DuplicateSkuException(request.sku());
        }

        product.update(
                request.name(),
                request.sku(),
                request.price()
        );

        return product;
    }
    @Transactional
    @CacheEvict(
        cacheNames = "products",
        key = "#id"
)
public void deleteProduct(Long id) {

    Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

    productRepository.delete(product);
}
}