package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.common.enums.AuthProvider;
import com.elhaffar.exoformbackend.common.enums.UserRole;
import com.elhaffar.exoformbackend.config.JwtUtils;
import com.elhaffar.exoformbackend.dto.auth.AuthResponseDTO;
import com.elhaffar.exoformbackend.dto.auth.LoginRequestDTO;
import com.elhaffar.exoformbackend.dto.auth.RegisterRequestDTO;
import com.elhaffar.exoformbackend.entities.User;
import com.elhaffar.exoformbackend.exceptions.BusinessException;
import com.elhaffar.exoformbackend.mapper.UserMapper;
import com.elhaffar.exoformbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerDto;
    private User newUser;

    @BeforeEach
    void setUp() {
        registerDto = new RegisterRequestDTO("john_doe", "john@test.com", "0612345678", "Tétouan", "password123");

        newUser = new User();
        newUser.setUsername("john_doe");
        newUser.setEmail("john@test.com");
    }

    @Test
    void register_shouldSucceed_whenEmailAndPhoneAreNew() {
        // ARRANGE
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("0612345678")).thenReturn(Optional.empty());
        when(userMapper.toEntityFromRegister(registerDto)).thenReturn(newUser);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("fake-access-token");
        when(jwtUtils.generateRefreshToken(anyString())).thenReturn("fake-refresh-token");
        when(jwtUtils.getExpirationTime()).thenReturn(900000L);

        // ACT
        AuthResponseDTO result = authService.register(registerDto);

        // ASSERT
        assertNotNull(result);
        assertEquals("john@test.com", result.email());
        assertEquals("john_doe", result.username());
        assertEquals("fake-access-token", result.accessToken());
        assertEquals(UserRole.CLIENT, result.role());
        verify(userRepository, times(1)).save(newUser);
        assertEquals(AuthProvider.LOCAL, newUser.getAuthProvider());
    }

    @Test
    void register_shouldThrowBusinessException_whenEmailAlreadyExists() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(new User()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(registerDto)
        );

        assertEquals("Cet email est déjà utilisé", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowBusinessException_whenPhoneAlreadyExists() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("0612345678")).thenReturn(Optional.of(new User()));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(registerDto)
        );

        assertEquals("Ce numéro de téléphone est déjà utilisé", exception.getMessage());
    }

    @Test
    void login_shouldSucceed_whenCredentialsAreCorrect() {
        // ARRANGE
        LoginRequestDTO loginDto = new LoginRequestDTO("john@test.com", "password123");

        User existingUser = new User();
        existingUser.setEmail("john@test.com");
        existingUser.setUsername("john_doe");
        existingUser.setPassword("hashed_password");
        existingUser.setRole(UserRole.CLIENT);

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
        when(jwtUtils.generateToken(anyString(), anyString())).thenReturn("fake-access-token");
        when(jwtUtils.generateRefreshToken(anyString())).thenReturn("fake-refresh-token");
        when(jwtUtils.getExpirationTime()).thenReturn(900000L);

        // ACT
        AuthResponseDTO result = authService.login(loginDto);

        // ASSERT
        assertNotNull(result);
        assertEquals("john@test.com", result.email());
        assertEquals("fake-access-token", result.accessToken());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsIncorrect() {
        LoginRequestDTO loginDto = new LoginRequestDTO("john@test.com", "wrong_password");

        User existingUser = new User();
        existingUser.setEmail("john@test.com");
        existingUser.setPassword("hashed_password");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(loginDto)
        );

        assertEquals("Mot de passe incorrect", exception.getMessage());
    }

    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {
        LoginRequestDTO loginDto = new LoginRequestDTO("unknown@test.com", "password123");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> authService.login(loginDto)
        );
    }
}