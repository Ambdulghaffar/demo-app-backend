package com.elhaffar.exoformbackend.dto.report;

import com.elhaffar.exoformbackend.common.enums.StockMovementType;

public record MovementTypeSummaryDTO(StockMovementType type, long count, long totalQuantity) {}
