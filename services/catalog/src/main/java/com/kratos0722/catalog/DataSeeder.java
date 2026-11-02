package com.kratos0722.catalog;

import com.kratos0722.catalog.service.CatalogService;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds a few demo products on startup so the gateway/orders demo has data.
 * Disabled under the {@code test} profile.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final CatalogService catalog;

    public DataSeeder(CatalogService catalog) {
        this.catalog = catalog;
    }

    @Override
    public void run(String... args) {
        if (!catalog.list().isEmpty()) {
            return;
        }
        catalog.create("SKU-LAPTOP", "14\" Laptop", "Lightweight dev laptop", new BigDecimal("1299.00"), 25);
        catalog.create("SKU-MOUSE", "Wireless Mouse", "Ergonomic mouse", new BigDecimal("39.90"), 200);
        catalog.create("SKU-DESK", "Standing Desk", "Electric sit/stand desk", new BigDecimal("499.00"), 15);
    }
}
