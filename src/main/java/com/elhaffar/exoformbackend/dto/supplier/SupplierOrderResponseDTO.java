package com.elhaffar.exoformbackend.dto.supplier;

import com.elhaffar.exoformbackend.common.enums.SupplierOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SupplierOrderResponseDTO(
        Integer id,
        Integer supplierId,
        String supplierName,
        SupplierOrderStatus status,
        List<SupplierOrderItemResponseDTO> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
