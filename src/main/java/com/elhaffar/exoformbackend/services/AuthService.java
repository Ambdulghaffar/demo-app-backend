package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.auth.*;

public interface AuthService {
    AuthResponseDTO register(RegisterRequestDTO dto);
    AuthResponseDTO login(LoginRequestDTO dto);
    AuthResponseDTO refreshToken(String refreshToken);
    AuthResponseDTO loginOrRegisterWithGoogle(GoogleAuthRequestDTO dto);
    MessageResponseDTO forgotPassword(ForgotPasswordRequestDTO dto);
    MessageResponseDTO resetPassword(ResetPasswordRequestDTO dto);
}
