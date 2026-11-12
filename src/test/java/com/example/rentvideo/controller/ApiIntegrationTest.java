package com.example.rentvideo.controller;

import com.example.rentvideo.entity.Role;
import com.example.rentvideo.entity.User;
import com.example.rentvideo.repository.UserRepository;
import com.example.rentvideo.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Runs the full app against in-memory H2 to check auth rules end to end
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired VideoRepository videoRepository;
    @Autowired PasswordEncoder encoder;

    private static final String VIDEO_JSON =
            "{\"title\":\"Inception\",\"director\":\"Nolan\",\"genre\":\"Sci-Fi\"}";

    @BeforeEach
    void setUp() {
        videoRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(new User("admin@test.com", encoder.encode("admin123"), "Ann", "Admin", Role.ADMIN));
        userRepository.save(new User("cust@test.com", encoder.encode("cust123"), "Carl", "Cust", Role.CUSTOMER));
    }

    @Test
    void register_isPublic_andDefaultsToCustomer() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@test.com\",\"password\":\"pass123\",\"firstName\":\"N\",\"lastName\":\"U\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cust@test.com\",\"password\":\"pass123\",\"firstName\":\"N\",\"lastName\":\"U\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cust@test.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_correctPassword_returnsUser() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cust@test.com\",\"password\":\"cust123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cust@test.com"));
    }

    @Test
    void listVideos_withoutAuth_returns401() throws Exception {
        mvc.perform(get("/api/videos")).andExpect(status().isUnauthorized());
    }

    @Test
    void listVideos_asCustomer_returns200() throws Exception {
        mvc.perform(get("/api/videos").with(httpBasic("cust@test.com", "cust123")))
                .andExpect(status().isOk());
    }

    @Test
    void createVideo_asCustomer_returns403() throws Exception {
        mvc.perform(post("/api/videos").with(httpBasic("cust@test.com", "cust123"))
                        .contentType(MediaType.APPLICATION_JSON).content(VIDEO_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void createVideo_asAdmin_returns201() throws Exception {
        mvc.perform(post("/api/videos").with(httpBasic("admin@test.com", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(VIDEO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getMissingVideo_returns404() throws Exception {
        mvc.perform(get("/api/videos/999").with(httpBasic("cust@test.com", "cust123")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createVideo_invalidBody_returns400() throws Exception {
        mvc.perform(post("/api/videos").with(httpBasic("admin@test.com", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
