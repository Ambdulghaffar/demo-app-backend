package com.elhaffar.exoformbackend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        String email
) {}
