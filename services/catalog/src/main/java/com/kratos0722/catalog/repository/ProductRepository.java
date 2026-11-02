package com.kratos0722.catalog.repository;

import com.kratos0722.catalog.domain.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);
}
