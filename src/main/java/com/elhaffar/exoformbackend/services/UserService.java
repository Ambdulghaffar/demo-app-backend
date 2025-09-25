package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.LoginDto;
import com.elhaffar.exoformbackend.dto.RegisterDto;
import com.elhaffar.exoformbackend.entities.User;
import com.elhaffar.exoformbackend.mapper.LoginMapper;
import com.elhaffar.exoformbackend.mapper.RegisterMapper;
import com.elhaffar.exoformbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class UserService implements UserServiceImpl{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginMapper loginMapper;
    @Autowired
    private RegisterMapper registerMapper;


    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Integer id, User user) {
        User userUpdate = userRepository.findById(id).orElse(null);
        if(userUpdate == null){
            throw new RuntimeException("User not found");
        }
        userUpdate.setUsername(user.getUsername());
        userUpdate.setEmail(user.getEmail());
        userUpdate.setAddress(user.getAddress());
        userUpdate.setPassword(user.getPassword());
        return userRepository.save(userUpdate);
    }

    @Override
    public void deleteUser(Integer id) {
        User userDelete = userRepository.findById(id).orElse(null);
        if(userDelete == null){
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User getUserById(Integer id) {
        User user = userRepository.findById(id).orElse(null);
        if(user == null){
            throw new RuntimeException("User not found");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User register(RegisterDto registerDto) {
        if(userRepository.findByEmail(registerDto.getEmail()).isPresent()){
            throw new RuntimeException("Email déjà utilisé");
        }
        User user = registerMapper.toEntity(registerDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public LoginDto login(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(()-> new RuntimeException("Utilisateur non trouvé"));

        if(!passwordEncoder.matches(loginDto.getPassword(),user.getPassword())){
            throw new RuntimeException("Mot de passe incorrect");
        }
        return loginMapper.toDto(user);

    }


}
