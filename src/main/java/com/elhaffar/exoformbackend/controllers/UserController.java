package com.elhaffar.exoformbackend.controllers;

import com.elhaffar.exoformbackend.dto.auth.MessageResponseDTO;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.user.ChangePasswordDTO;
import com.elhaffar.exoformbackend.dto.user.MeUpdateDTO;
import com.elhaffar.exoformbackend.dto.user.UserRequestDTO;
import com.elhaffar.exoformbackend.dto.user.UserResponseDTO;
import com.elhaffar.exoformbackend.dto.user.UserStatsDTO;
import com.elhaffar.exoformbackend.dto.user.UserUpdateDTO;
import com.elhaffar.exoformbackend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PageResponseDTO<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "id")   String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false)      String role,
            @RequestParam(required = false)      String search
    ) {
        return ResponseEntity.ok(
                userService.getAllUsers(page, size, sortBy, sortDir, role, search)
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequestDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserStatsDTO> getUserStats() {
        return ResponseEntity.ok(userService.getUserStats());
    }

    // ── Profil personnel (tout utilisateur authentifié) ───────────────────────

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMe(
            Authentication authentication,
            @Valid @RequestBody MeUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateCurrentUser(authentication.getName(), dto));
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponseDTO> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDTO dto) {
        return ResponseEntity.ok(userService.changePassword(authentication.getName(), dto));
    }
}
