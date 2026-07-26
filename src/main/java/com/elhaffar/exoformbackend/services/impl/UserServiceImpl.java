package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.common.enums.AuthProvider;
import com.elhaffar.exoformbackend.common.utils.SortUtils;
import com.elhaffar.exoformbackend.dto.auth.MessageResponseDTO;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.user.ChangePasswordDTO;
import com.elhaffar.exoformbackend.dto.user.MeUpdateDTO;
import com.elhaffar.exoformbackend.dto.user.UserRequestDTO;
import com.elhaffar.exoformbackend.dto.user.UserResponseDTO;
import com.elhaffar.exoformbackend.dto.user.UserStatsDTO;
import com.elhaffar.exoformbackend.dto.user.UserUpdateDTO;
import com.elhaffar.exoformbackend.entities.User;
import com.elhaffar.exoformbackend.common.enums.UserRole;
import com.elhaffar.exoformbackend.exceptions.BusinessException;
import com.elhaffar.exoformbackend.exceptions.ResourceNotFoundException;
import com.elhaffar.exoformbackend.mapper.UserMapper;
import com.elhaffar.exoformbackend.repository.UserRepository;
import com.elhaffar.exoformbackend.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResponseDTO<UserResponseDTO> getAllUsers(
            int page, int size, String sortBy, String sortDir,
            String role, String search) {

        Pageable pageable = PageRequest.of(page, size, SortUtils.buildSort(sortBy, sortDir));

        boolean hasSearch = search != null && !search.isBlank();
        boolean hasRole   = role   != null && !role.isBlank() && !role.equalsIgnoreCase("all");

        Page<User> result;

        if (hasSearch) {
            // Si recherche active → ignore le filtre rôle (UX plus naturelle)
            result = userRepository.searchUsers(search, pageable);
        } else if (hasRole) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                result = userRepository.findByRole(userRole, pageable);
            } catch (IllegalArgumentException e) {
                result = userRepository.findAll(pageable);
            }
        } else {
            result = userRepository.findAll(pageable);
        }

        return PageResponseDTO.from(result.map(userMapper::toResponseDTO));
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        // BusinessException au lieu de RuntimeException → 409 Conflict
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException("Cet email est déjà utilisé");
        }
        if (userRepository.findByPhone(dto.phone()).isPresent()) {
            throw new BusinessException("Ce numéro de téléphone est déjà utilisé");
        }

        User user = userMapper.toEntity(dto);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setPassword(passwordEncoder.encode(dto.password()));

        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO updateUser(Integer id, UserUpdateDTO dto) {
        // ResourceNotFoundException → 404
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        // Vérifie les doublons en excluant l'utilisateur courant
        userRepository.findByEmail(dto.email())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new BusinessException("Cet email est déjà utilisé"); });

        userRepository.findByPhone(dto.phone())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> { throw new BusinessException("Ce numéro de téléphone est déjà utilisé"); });

        userMapper.updateUserFromDto(dto, existing);
        return userMapper.toResponseDTO(userRepository.save(existing));
    }

    @Override
    public void deleteUser(Integer id) {
        // ResourceNotFoundException → 404
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponseDTO getUserById(Integer id) {
        // ResourceNotFoundException → 404
        return userRepository.findById(id)
                .map(userMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    @Override
    public UserStatsDTO getUserStats() {
        return new UserStatsDTO(
                userRepository.count(),
                userRepository.countByRole(UserRole.ADMIN),
                userRepository.countByRole(UserRole.MANAGER),
                userRepository.countByRole(UserRole.CLIENT)
        );
    }

    @Override
    public UserResponseDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", email));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateCurrentUser(String email, MeUpdateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", email));

        if (dto.phone() != null && !dto.phone().isBlank()) {
            userRepository.findByPhone(dto.phone())
                    .filter(u -> !u.getId().equals(user.getId()))
                    .ifPresent(u -> { throw new BusinessException("Ce numéro de téléphone est déjà utilisé"); });
        }

        userMapper.updateMeFromDto(dto, user);
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    public MessageResponseDTO changePassword(String email, ChangePasswordDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", email));

        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new BusinessException("Ce compte utilise la connexion Google, aucun mot de passe à modifier ici");
        }

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new BusinessException("Le mot de passe actuel est incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        return new MessageResponseDTO("Mot de passe mis à jour avec succès.");
    }
}