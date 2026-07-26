package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.auth.MessageResponseDTO;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.user.ChangePasswordDTO;
import com.elhaffar.exoformbackend.dto.user.MeUpdateDTO;
import com.elhaffar.exoformbackend.dto.user.UserRequestDTO;
import com.elhaffar.exoformbackend.dto.user.UserResponseDTO;
import com.elhaffar.exoformbackend.dto.user.UserStatsDTO;
import com.elhaffar.exoformbackend.dto.user.UserUpdateDTO;

public interface UserService {
    PageResponseDTO<UserResponseDTO> getAllUsers(int page, int size, String sortBy, String sortDir, String role , String search);
    UserResponseDTO createUser(UserRequestDTO userRequestDTO);
    UserResponseDTO updateUser(Integer id, UserUpdateDTO userUpdateDTO);
    void deleteUser(Integer id);
    UserResponseDTO getUserById(Integer id);
    UserStatsDTO getUserStats();
    UserResponseDTO getCurrentUser(String email);
    UserResponseDTO updateCurrentUser(String email, MeUpdateDTO dto);
    MessageResponseDTO changePassword(String email, ChangePasswordDTO dto);
}
