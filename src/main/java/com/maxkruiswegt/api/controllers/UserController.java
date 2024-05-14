package com.maxkruiswegt.api.controllers;

import com.maxkruiswegt.api.models.account.User;
import com.maxkruiswegt.api.models.dto.LoginRequest;
import com.maxkruiswegt.api.models.dto.UserDto;
import com.maxkruiswegt.api.security.JwtProvider;
import com.maxkruiswegt.api.services.PasswordService;
import com.maxkruiswegt.api.services.UserService;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final PasswordService passwordService;

    public UserController(UserService userService, JwtProvider jwtProvider, PasswordService passwordService) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.passwordService = passwordService;
    }

    @GetMapping("/users/me")
    public ResponseEntity<Map<String, Object>> validateToken(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(Map.of("user", new UserDto(user)));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDto> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return users.stream().map(UserDto::new).toList();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody LoginRequest loginRequest) {
        // Sanitize the user's details
        loginRequest.setEmail(StringEscapeUtils.escapeHtml4(loginRequest.getEmail().trim().toLowerCase()));

        // Validate the user's details
        // https://emailregex.com/index.html (RFC 5322 Official Standard)
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty() || !loginRequest.getEmail().matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid email is required"));
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        // Find the user by email
        User user = userService.getUserByEmail(loginRequest.getEmail());

        // Check if the user exists and then check the password
        if (user == null || !passwordService.checkPassword(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
        }

        // Create a JWT token for the user
        String token = jwtProvider.createToken(user);

        // Return the user and the token
        return ResponseEntity.ok(Map.of("user", new UserDto(user), "token", token));
    }
}
