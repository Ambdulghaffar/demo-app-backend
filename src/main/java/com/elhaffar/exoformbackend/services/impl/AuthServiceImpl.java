package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.dto.auth.*;
import com.elhaffar.exoformbackend.entities.PasswordResetToken;
import com.elhaffar.exoformbackend.entities.User;
import com.elhaffar.exoformbackend.common.enums.AuthProvider;
import com.elhaffar.exoformbackend.common.enums.UserRole;
import com.elhaffar.exoformbackend.exceptions.BusinessException;
import com.elhaffar.exoformbackend.mapper.UserMapper;
import com.elhaffar.exoformbackend.repository.PasswordResetTokenRepository;
import com.elhaffar.exoformbackend.repository.UserRepository;
import com.elhaffar.exoformbackend.config.JwtUtils;
import com.elhaffar.exoformbackend.services.AuthService;
import com.elhaffar.exoformbackend.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            UserMapper userMapper,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException("Cet email est déjà utilisé");
        }
        if (userRepository.findByPhone(dto.phone()).isPresent()) {
            throw new BusinessException("Ce numéro de téléphone est déjà utilisé");
        }
        User user = userMapper.toEntityFromRegister(dto);
        user.setRole(UserRole.CLIENT);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setPassword(passwordEncoder.encode(dto.password()));
        userRepository.save(user);

        String accessToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                jwtUtils.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        String accessToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                jwtUtils.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public AuthResponseDTO loginOrRegisterWithGoogle(GoogleAuthRequestDTO dto) {
        Optional<User> existingUser = userRepository.findByEmail(dto.email());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getAuthProvider() == AuthProvider.LOCAL) {
                throw new BusinessException("Cet email est déjà utilisé");
            }

            String accessToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
            String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
            return new AuthResponseDTO(
                    accessToken,
                    refreshToken,
                    jwtUtils.getExpirationTime(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );
        }

        User newUser = new User();
        newUser.setEmail(dto.email());
        newUser.setUsername(dto.username());
        newUser.setRole(UserRole.CLIENT);
        newUser.setAuthProvider(AuthProvider.GOOGLE);
        newUser.setPassword(null);
        userRepository.save(newUser);

        String accessToken = jwtUtils.generateToken(newUser.getEmail(), newUser.getRole().name());
        String refreshToken = jwtUtils.generateRefreshToken(newUser.getEmail());
        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                jwtUtils.getExpirationTime(),
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getRole()
        );
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        if (!jwtUtils.isTokenValid(refreshToken)) {
            throw new RuntimeException("Refresh Token expiré ou invalide");
        }
        String email = jwtUtils.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String newAccessToken = jwtUtils.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponseDTO(
                newAccessToken,
                refreshToken,
                jwtUtils.getExpirationTime(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Override
    public MessageResponseDTO forgotPassword(ForgotPasswordRequestDTO dto) {
        String genericMessage = "Si un compte existe avec cet email, un lien de réinitialisation vient d'être envoyé.";

        Optional<User> userOpt = userRepository.findByEmail(dto.email());

        if (userOpt.isPresent() && userOpt.get().getAuthProvider() == AuthProvider.LOCAL) {
            User user = userOpt.get();

            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
        }

        return new MessageResponseDTO(genericMessage);
    }

    @Override
    public MessageResponseDTO resetPassword(ResetPasswordRequestDTO dto) {
        String tokenHash = hashToken(dto.token());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Lien de réinitialisation invalide ou expiré"));

        if (resetToken.isUsed()) {
            throw new BusinessException("Ce lien de réinitialisation a déjà été utilisé");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Ce lien de réinitialisation a expiré");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponseDTO("Votre mot de passe a été réinitialisé avec succès.");
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 non disponible", e);
        }
    }
}
