package com.kratos0722.catalog.service;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String sku) {
        super("No product found for SKU '" + sku + "'");
    }
}
