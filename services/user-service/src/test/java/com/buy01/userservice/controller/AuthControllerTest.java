package com.buy01.userservice.controller;

import com.buy01.userservice.dto.AuthRequest;
import com.buy01.userservice.dto.AuthResponse;
import com.buy01.userservice.dto.UpdateUserRequest;
import com.buy01.userservice.dto.UserProfileResponse;
import com.buy01.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.buy01.userservice.security.JwtUtil;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password");
        authRequest.setRole("CLIENT");

        authResponse = new AuthResponse("mockToken", "CLIENT", "test@example.com");
    }

    @Test
    @WithMockUser
    void registerShouldReturnToken() throws Exception {
        given(userService.register(any(AuthRequest.class))).willReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockToken"));
    }

    @Test
    @WithMockUser
    void loginShouldReturnToken() throws Exception {
        given(userService.login(any(AuthRequest.class))).willReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockToken"));
    }

    @Test
    @WithMockUser
    void logoutShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfileShouldReturnOk() throws Exception {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setCity("New City");

        mockMvc.perform(put("/api/auth/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(userService).updateUser(eq("test@example.com"), any(UpdateUserRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getProfileShouldReturnProfile() throws Exception {
        UserProfileResponse profile = new UserProfileResponse();
        profile.setEmail("test@example.com");

        given(userService.getProfile("test@example.com")).willReturn(profile);

        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
