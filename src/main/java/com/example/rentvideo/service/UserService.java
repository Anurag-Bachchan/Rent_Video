package com.example.rentvideo.service;

import com.example.rentvideo.dto.RegisterRequest;
import com.example.rentvideo.dto.UserResponse;
import com.example.rentvideo.entity.Role;
import com.example.rentvideo.entity.User;
import com.example.rentvideo.exception.EmailAlreadyExistsException;
import com.example.rentvideo.exception.ResourceNotFoundException;
import com.example.rentvideo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        Role role = req.role() != null ? req.role() : Role.CUSTOMER;
        User user = new User(email, passwordEncoder.encode(req.password()),
                req.firstName(), req.lastName(), role);
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
