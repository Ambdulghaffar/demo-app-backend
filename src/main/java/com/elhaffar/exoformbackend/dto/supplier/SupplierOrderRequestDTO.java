package com.elhaffar.exoformbackend.dto.supplier;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SupplierOrderRequestDTO(
        @NotNull(message = "Le fournisseur est obligatoire") Integer supplierId,
        @NotEmpty(message = "La commande doit contenir au moins un article") @Valid
        List<SupplierOrderItemRequestDTO> items
) {}
