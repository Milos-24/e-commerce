package com.commerce.auth.controller;

import com.commerce.auth.jwt.JwtUtil;
import com.commerce.auth.model.LoginRequest;
import com.commerce.auth.model.User;
import com.commerce.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private static final String DUMMY_BCRYPT = "$2a$10$7EqJtq98hPqEX7fNZaFWoO";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }

        Optional<User> maybeUser = userRepository.findByUsername(request.getUsername());

        if (maybeUser.isEmpty()) {
            passwordEncoder.matches(request.getPassword(), DUMMY_BCRYPT);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        User user = maybeUser.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        String token = jwtUtil.generateToken(user.getUsername(), List.of("ROLE_ADMIN"));
        return ResponseEntity.ok(Map.of("token", token));
    }
}
