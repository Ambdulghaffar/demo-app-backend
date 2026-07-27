package com.elhaffar.exoformbackend.dto.stock;

import com.elhaffar.exoformbackend.common.enums.StockMovementType;

import java.time.LocalDateTime;

public record StockMovementResponseDTO(
        Integer id,
        Integer productId,
        String productName,
        StockMovementType type,
        Integer quantity,
        String reason,
        Integer orderId,
        LocalDateTime createdAt
) {}
