package com.elhaffar.exoformbackend.dto.order;

import com.elhaffar.exoformbackend.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Integer id,
        String customerUsername,
        String shippingAddress,
        OrderStatus status,
        List<OrderItemResponseDTO> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
