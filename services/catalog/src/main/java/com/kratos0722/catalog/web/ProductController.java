package com.kratos0722.catalog.web;

import com.kratos0722.catalog.domain.Product;
import com.kratos0722.catalog.service.CatalogService;
import com.kratos0722.catalog.web.dto.CreateProductRequest;
import com.kratos0722.catalog.web.dto.ProductResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog/products")
public class ProductController {

    private final CatalogService service;

    public ProductController(CatalogService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        Product p = service.create(request.sku(), request.name(), request.description(),
                request.price(), request.stock());
        return ResponseEntity.created(URI.create("/api/catalog/products/" + p.getSku()))
                .body(ProductResponse.from(p));
    }

    @GetMapping("/{sku}")
    public ProductResponse get(@PathVariable String sku) {
        return ProductResponse.from(service.getBySku(sku));
    }

    @GetMapping
    public List<ProductResponse> list() {
        return service.list().stream().map(ProductResponse::from).toList();
    }
}
