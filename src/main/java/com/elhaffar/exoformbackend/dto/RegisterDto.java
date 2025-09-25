package com.elhaffar.exoformbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterDto {
    private String username;
    private String email;
    private String address;
    private String password;
}
