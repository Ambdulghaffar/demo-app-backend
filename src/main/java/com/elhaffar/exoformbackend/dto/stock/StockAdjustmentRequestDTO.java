package com.elhaffar.exoformbackend.dto.stock;

import com.elhaffar.exoformbackend.common.enums.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequestDTO(
        @NotNull(message = "Le produit est obligatoire") Integer productId,

        @NotNull(message = "Le type d'ajustement est obligatoire") StockMovementType type,

        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être d'au moins 1") Integer quantity,

        @NotBlank(message = "Le motif est obligatoire") String reason
) {}
