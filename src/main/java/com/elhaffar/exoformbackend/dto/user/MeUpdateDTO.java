package com.elhaffar.exoformbackend.dto.user;

import jakarta.validation.constraints.NotBlank;

public record MeUpdateDTO(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        String username,
        String phone,
        String address,
        String imageUrl
) {}
