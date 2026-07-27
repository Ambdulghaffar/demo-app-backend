package com.elhaffar.exoformbackend.dto.order;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        Integer productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {}
