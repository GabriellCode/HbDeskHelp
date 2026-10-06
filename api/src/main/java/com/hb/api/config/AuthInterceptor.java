package com.hb.api.config;

import com.hb.api.model.User;
import com.hb.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public AuthInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().startsWith("/api/auth") || request.getRequestURI().startsWith("/images") || request.getRequestURI().equals("/index.html") || request.getRequestURI().endsWith(".css") || request.getRequestURI().endsWith(".js") || request.getRequestURI().equals("/")) {
            return true; // Rotas públicas
        }

        if (request.getRequestURI().startsWith("/api/")) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Optional<User> userOpt = userRepository.findByToken(token);
                if (userOpt.isPresent()) {
                    request.setAttribute("user", userOpt.get());
                    return true;
                }
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }
}
