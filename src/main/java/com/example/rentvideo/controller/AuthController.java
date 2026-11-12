package com.example.rentvideo.controller;

import com.example.rentvideo.dto.LoginRequest;
import com.example.rentvideo.dto.RegisterRequest;
import com.example.rentvideo.dto.UserResponse;
import com.example.rentvideo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // Public: create an account
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    // Public: check credentials. The API is stateless, so after this the client
    // sends the same email/password as a Basic Auth header on every private request.
    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        return userService.getByEmail(auth.getName());
    }

    // Private: who am I (uses the Basic Auth header)
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }
}
