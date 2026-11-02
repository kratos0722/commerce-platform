package com.kratos0722.catalog.web.dto;

import com.kratos0722.catalog.domain.Product;
import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        int stock) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(),
                p.getDescription(), p.getPrice(), p.getStock());
    }
}
