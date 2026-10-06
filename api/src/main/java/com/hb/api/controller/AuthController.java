package com.hb.api.controller;

import com.hb.api.model.User;
import com.hb.api.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            userRepository.save(user);

            return Map.of("success", true, "token", token, "role", user.getRole(), "username", user.getUsername(), "id", user.getId());
        }

        return Map.of("success", false, "message", "Credenciais inválidas");
    }
}
