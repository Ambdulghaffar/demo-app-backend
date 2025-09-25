package com.elhaffar.exoformbackend.mapper;

import com.elhaffar.exoformbackend.dto.RegisterDto;
import com.elhaffar.exoformbackend.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RegisterMapper {
    RegisterDto toDto(User user);
    User toEntity(RegisterDto registerDto);
}
