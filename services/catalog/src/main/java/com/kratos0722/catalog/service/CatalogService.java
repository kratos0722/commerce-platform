package com.kratos0722.catalog.service;

import com.kratos0722.catalog.domain.Product;
import com.kratos0722.catalog.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final ProductRepository products;

    public CatalogService(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    public Product create(String sku, String name, String description, BigDecimal price, int stock) {
        if (products.existsBySku(sku)) {
            throw new DuplicateSkuException(sku);
        }
        return products.save(new Product(sku, name, description, price, stock));
    }

    @Transactional(readOnly = true)
    public Product getBySku(String sku) {
        return products.findBySku(sku).orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @Transactional(readOnly = true)
    public List<Product> list() {
        return products.findAll();
    }
}
