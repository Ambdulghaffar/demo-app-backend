package com.elhaffar.exoformbackend.mapper;

import com.elhaffar.exoformbackend.dto.auth.RegisterRequestDTO;
import com.elhaffar.exoformbackend.dto.user.MeUpdateDTO;
import com.elhaffar.exoformbackend.dto.user.UserRequestDTO;
import com.elhaffar.exoformbackend.dto.user.UserResponseDTO;
import com.elhaffar.exoformbackend.dto.user.UserUpdateDTO;
import com.elhaffar.exoformbackend.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 1. Transformation Unique (Détails d'un utilisateur)
    UserResponseDTO toResponseDTO(User user);

    // 2. Transformation de Liste (Pour le "Afficher tout")
    List<UserResponseDTO> toResponseDTOList(List<User> users);

    // 3. Création (DTO -> Nouvelle Entité)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRequestDTO dto);

    // 4. Pour l'Authentification (Register)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true) // On ignore pour hacher manuellement dans le service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntityFromRegister(RegisterRequestDTO dto);

    /**
     * 5. Mise à jour (DTO -> Entité existante)
     * Met à jour l'entité existante avec les données du DTO
     * @MappingTarget indique à MapStruct de modifier l'objet existant
     * au lieu d'en créer un nouveau.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // password jamais touché
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromDto(UserUpdateDTO dto, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "authProvider", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMeFromDto(MeUpdateDTO dto, @MappingTarget User user);

}
