package com.elhaffar.exoformbackend.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(
        @NotNull(message = "Le produit est obligatoire") Integer productId,
        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 1, message = "La quantité doit être d'au moins 1") Integer quantity
) {}
