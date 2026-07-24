package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.dto.auth.AuthResponseDTO;
import com.elhaffar.exoformbackend.dto.auth.GoogleAuthRequestDTO;
import com.elhaffar.exoformbackend.dto.auth.LoginRequestDTO;
import com.elhaffar.exoformbackend.dto.auth.RegisterRequestDTO;
import com.elhaffar.exoformbackend.entities.User;
import com.elhaffar.exoformbackend.common.enums.AuthProvider;
import com.elhaffar.exoformbackend.common.enums.UserRole;
import com.elhaffar.exoformbackend.exceptions.BusinessException;
import com.elhaffar.exoformbackend.mapper.UserMapper;
import com.elhaffar.exoformbackend.repository.UserRepository;
import com.elhaffar.exoformbackend.config.JwtUtils;
import com.elhaffar.exoformbackend.services.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if(userRepository.findByEmail(dto.email()).isPresent()){
            throw new BusinessException("Cet email est déjà utilisé");
        }
        if(userRepository.findByPhone(dto.phone()).isPresent()){
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

        String accessToken = jwtUtils.generateToken(user.getEmail(),user.getRole().name());
        String refreshToken =  jwtUtils.generateRefreshToken(user.getEmail());

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
        if(!jwtUtils.isTokenValid(refreshToken)){
            throw new RuntimeException("Refresh Token expiré ou invalide");
        }
         String email = jwtUtils.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Utilisateur non trouvé"));

        // On génère un nouvel Access Token, mais on garde le même Refresh Token (ou on en génère un nouveau)
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
}
