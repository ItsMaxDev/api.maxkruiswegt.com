package com.maxkruiswegt.api.services;

import com.maxkruiswegt.api.models.account.User;
import com.maxkruiswegt.api.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public User saveUser(User user) {
        // Hash the password before saving it to the database
        user.setPassword(passwordService.hashPassword(user.getPassword()));

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public boolean isEmailTaken(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
