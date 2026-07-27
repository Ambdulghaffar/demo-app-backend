package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.common.enums.StockMovementType;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.stock.StockAdjustmentRequestDTO;
import com.elhaffar.exoformbackend.dto.stock.StockMovementResponseDTO;

public interface StockMovementService {

    PageResponseDTO<StockMovementResponseDTO> getMovements(int page, int size, Integer productId, StockMovementType type);

    StockMovementResponseDTO adjustStock(String requesterEmail, StockAdjustmentRequestDTO dto);
}
