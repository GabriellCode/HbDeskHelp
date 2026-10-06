package com.hb.api.controller;

import com.hb.api.model.User;
import com.hb.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private boolean isChief(HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        return user != null && "CHEFE_TI".equals(user.getRole());
    }

    @GetMapping
    public List<User> listUsers(HttpServletRequest request) {
        if (!isChief(request)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Acesso negado");
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        if (!isChief(request)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Acesso negado");
        User user = new User();
        user.setUsername(payload.get("username"));
        user.setPassword(payload.get("password"));
        user.setRole(payload.get("role"));
        return userRepository.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isChief(request)) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Acesso negado");
        userRepository.deleteById(id);
    }
}
