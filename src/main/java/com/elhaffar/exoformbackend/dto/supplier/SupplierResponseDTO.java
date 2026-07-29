package com.elhaffar.exoformbackend.dto.supplier;

import java.time.LocalDateTime;

public record SupplierResponseDTO(
        Integer id,
        String name,
        String contactName,
        String email,
        String phone,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
