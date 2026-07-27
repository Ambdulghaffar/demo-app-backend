package com.elhaffar.exoformbackend.dto.auth;

import com.elhaffar.exoformbackend.common.enums.UserRole;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String username,
        String email,
        UserRole role,
        String imageUrl
) {}
