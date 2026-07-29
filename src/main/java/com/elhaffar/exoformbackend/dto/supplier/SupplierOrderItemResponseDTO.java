package com.elhaffar.exoformbackend.dto.supplier;

import java.math.BigDecimal;

public record SupplierOrderItemResponseDTO(
        Integer productId,
        String productName,
        BigDecimal unitCost,
        Integer quantity,
        BigDecimal subtotal
) {}
