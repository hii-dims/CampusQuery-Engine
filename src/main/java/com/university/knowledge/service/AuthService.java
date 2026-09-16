package com.university.knowledge.service;

import com.university.knowledge.entity.User;
import com.university.knowledge.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(
            String username,
            String password) {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(password);

        User user =
                new User(
                        username,
                        encodedPassword,
                        "USER"
                );

        return userRepository.save(user);
    }
}
