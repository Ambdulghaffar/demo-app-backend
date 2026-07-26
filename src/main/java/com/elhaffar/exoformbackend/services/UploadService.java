package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.upload.UploadSignatureResponseDTO;

public interface UploadService {
    UploadSignatureResponseDTO generateSignature(String folder);
}
