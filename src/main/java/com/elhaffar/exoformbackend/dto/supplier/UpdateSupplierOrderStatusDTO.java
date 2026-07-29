package com.elhaffar.exoformbackend.dto.supplier;

import com.elhaffar.exoformbackend.common.enums.SupplierOrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSupplierOrderStatusDTO(
        @NotNull SupplierOrderStatus status
) {}
