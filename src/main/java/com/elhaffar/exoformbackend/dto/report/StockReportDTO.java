package com.elhaffar.exoformbackend.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record StockReportDTO(
        BigDecimal totalStockValue,
        long totalProducts,
        long lowStockCount,
        long outOfStockCount,
        List<LowStockProductDTO> lowStockProducts,
        List<MovementTypeSummaryDTO> movementsSummary
) {}
