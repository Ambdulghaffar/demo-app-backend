package com.elhaffar.exoformbackend.controllers;

import com.elhaffar.exoformbackend.common.enums.StockMovementType;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.stock.StockAdjustmentRequestDTO;
import com.elhaffar.exoformbackend.dto.stock.StockMovementResponseDTO;
import com.elhaffar.exoformbackend.services.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<PageResponseDTO<StockMovementResponseDTO>> getMovements(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    Integer productId,
            @RequestParam(required = false)    StockMovementType type) {
        return ResponseEntity.ok(stockMovementService.getMovements(page, size, productId, type));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<StockMovementResponseDTO> adjustStock(
            Authentication authentication,
            @Valid @RequestBody StockAdjustmentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.adjustStock(authentication.getName(), dto));
    }
}
