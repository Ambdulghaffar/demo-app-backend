package com.elhaffar.exoformbackend.controllers;

import com.elhaffar.exoformbackend.dto.LoginDto;
import com.elhaffar.exoformbackend.dto.RegisterDto;
import com.elhaffar.exoformbackend.entities.User;
import com.elhaffar.exoformbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserControllers {

    @Autowired
    private UserService userService;

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable("id") Integer id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Integer id) {
        userService.deleteUser(id);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable("id") Integer id) {
        return userService.getUserById(id);
    }
    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterDto registerDto) {
        return userService.register(registerDto);
    }
    @PostMapping("/login")
    public LoginDto login(@RequestBody LoginDto loginDto) {
        return userService.login(loginDto);
    }

}
