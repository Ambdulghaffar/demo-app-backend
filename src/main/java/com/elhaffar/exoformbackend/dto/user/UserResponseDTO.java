package com.elhaffar.exoformbackend.dto.user;

import com.elhaffar.exoformbackend.common.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Integer id,
        String username,
        String email,
        String phone,
        String address,
        String imageUrl,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
