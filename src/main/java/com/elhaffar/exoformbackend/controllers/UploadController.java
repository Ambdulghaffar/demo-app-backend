package com.elhaffar.exoformbackend.controllers;

import com.elhaffar.exoformbackend.dto.upload.UploadSignatureResponseDTO;
import com.elhaffar.exoformbackend.services.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/signature")
    public ResponseEntity<UploadSignatureResponseDTO> getSignature(
            @RequestParam(defaultValue = "avatars") String folder) {
        return ResponseEntity.ok(uploadService.generateSignature(folder));
    }
}
