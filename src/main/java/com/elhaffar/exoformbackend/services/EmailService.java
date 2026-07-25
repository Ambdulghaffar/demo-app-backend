package com.elhaffar.exoformbackend.services;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String username, String resetLink);
}
