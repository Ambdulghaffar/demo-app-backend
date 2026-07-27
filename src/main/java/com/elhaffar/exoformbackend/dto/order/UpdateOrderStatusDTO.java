package com.elhaffar.exoformbackend.dto.order;

import com.elhaffar.exoformbackend.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusDTO(
        @NotNull(message = "Le statut est obligatoire") OrderStatus status
) {}
