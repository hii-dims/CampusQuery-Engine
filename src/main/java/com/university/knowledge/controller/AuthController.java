package com.university.knowledge.controller;

import com.university.knowledge.dto.RegisterRequest;
import com.university.knowledge.dto.UserResponse;
import com.university.knowledge.entity.User;
import com.university.knowledge.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request) {

        User user =
                authService.register(
                        request.username(),
                        request.password()
                );

        return UserResponse.from(user);
    }
}
