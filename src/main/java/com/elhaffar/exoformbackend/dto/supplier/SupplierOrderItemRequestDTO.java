package com.elhaffar.exoformbackend.dto.supplier;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SupplierOrderItemRequestDTO(
        @NotNull Integer productId,
        @NotNull @Min(1) Integer quantity,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal unitCost
) {}
