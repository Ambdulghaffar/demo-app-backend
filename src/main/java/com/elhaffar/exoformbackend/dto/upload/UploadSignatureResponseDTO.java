package com.elhaffar.exoformbackend.dto.upload;

public record UploadSignatureResponseDTO(
        String signature,
        long timestamp,
        String apiKey,
        String cloudName,
        String folder
) {}
