package com.elhaffar.exoformbackend.mapper;

import com.elhaffar.exoformbackend.dto.LoginDto;
import com.elhaffar.exoformbackend.entities.User;
import org.mapstruct.Mapper;

import java.awt.*;

@Mapper(componentModel="spring")
public interface LoginMapper {
    LoginDto toDto(User user);
    User toEntity(LoginDto loginDto);
}
