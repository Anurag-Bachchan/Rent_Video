package com.example.rentvideo.service;

import com.example.rentvideo.dto.RegisterRequest;
import com.example.rentvideo.dto.UserResponse;
import com.example.rentvideo.entity.Role;
import com.example.rentvideo.entity.User;
import com.example.rentvideo.exception.EmailAlreadyExistsException;
import com.example.rentvideo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void register_hashesPassword_andDefaultsToCustomer() {
        RegisterRequest req = new RegisterRequest("john@test.com", "secret123", "John", "Doe", null);

        UserResponse res = userService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals(Role.CUSTOMER, res.role());
        assertNotEquals("secret123", saved.getPassword());
        assertTrue(passwordEncoder.matches("secret123", saved.getPassword()));
    }

    @Test
    void register_keepsGivenRole() {
        RegisterRequest req = new RegisterRequest("admin@test.com", "secret123", "Ann", "Admin", Role.ADMIN);
        assertEquals(Role.ADMIN, userService.register(req).role());
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);
        RegisterRequest req = new RegisterRequest("john@test.com", "secret123", "John", "Doe", null);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.register(req));
        verify(userRepository, never()).save(any());
    }
}
