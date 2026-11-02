package com.kratos0722.catalog.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 1024) String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @Min(0) int stock) {
}
