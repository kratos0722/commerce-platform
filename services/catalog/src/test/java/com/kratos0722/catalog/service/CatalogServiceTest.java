package com.kratos0722.catalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kratos0722.catalog.domain.Product;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CatalogServiceTest {

    @Autowired
    private CatalogService service;

    @Test
    void create_thenGetBySku() {
        service.create("S-1", "Widget", "d", new BigDecimal("9.99"), 10);
        Product p = service.getBySku("S-1");
        assertEquals("Widget", p.getName());
        assertEquals(10, p.getStock());
    }

    @Test
    void create_duplicateSku_throws() {
        service.create("S-DUP", "Widget", null, new BigDecimal("1.00"), 1);
        assertThrows(DuplicateSkuException.class,
                () -> service.create("S-DUP", "Widget2", null, new BigDecimal("2.00"), 2));
    }

    @Test
    void getBySku_unknown_throws() {
        assertThrows(ProductNotFoundException.class, () -> service.getBySku("MISSING"));
    }
}
