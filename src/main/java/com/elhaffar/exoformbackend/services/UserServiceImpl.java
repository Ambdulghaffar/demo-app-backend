package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.LoginDto;
import com.elhaffar.exoformbackend.dto.RegisterDto;
import com.elhaffar.exoformbackend.entities.User;

import java.util.List;

public interface UserServiceImpl{

    User createUser(User user);
    User updateUser(Integer id, User user);
    void deleteUser(Integer id);
    User getUserById(Integer id);
    List<User> getAllUsers();
    User register(RegisterDto registerDto);
    LoginDto login(LoginDto loginDto);

}
