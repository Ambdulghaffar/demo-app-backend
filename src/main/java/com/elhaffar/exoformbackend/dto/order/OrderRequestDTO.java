package com.elhaffar.exoformbackend.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequestDTO(
        @NotBlank(message = "L'adresse de livraison est obligatoire") String shippingAddress,
        @NotEmpty(message = "La commande doit contenir au moins un article")
        @Valid List<OrderItemRequestDTO> items
) {}
