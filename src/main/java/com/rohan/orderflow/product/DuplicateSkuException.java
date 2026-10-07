package com.rohan.orderflow.product;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) {
        super("Product already exists with sku: " + sku);
    }
}