package com.elhaffar.exoformbackend.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SupplierRequestDTO(
        @NotBlank(message = "Le nom est obligatoire") String name,
        String contactName,
        @Email(message = "Email invalide") String email,
        String phone,
        String address
) {}
