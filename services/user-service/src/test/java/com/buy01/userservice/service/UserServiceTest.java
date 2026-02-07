package com.buy01.userservice.service;

import com.buy01.userservice.dto.AuthRequest;
import com.buy01.userservice.dto.AuthResponse;
import com.buy01.userservice.dto.Role;
import com.buy01.userservice.dto.UpdateUserRequest;
import com.buy01.userservice.model.User;
import com.buy01.userservice.repository.UserRepository;
import com.buy01.userservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "encodedPassword", Role.CLIENT);
        testUser.setId("1");
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        AuthRequest request = new AuthRequest("test@example.com", "password", Role.CLIENT);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), any(Role.class), any())).thenReturn("mockToken");

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        AuthRequest request = new AuthRequest("test@example.com", "password", null);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getEmail(), testUser.getRole(), testUser.getId())).thenReturn("mockToken");

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
    }

    @Test
    void updateUser_shouldUpdateAvailableFields() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setCity("New City");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        userService.updateUser("test@example.com", updateRequest);

        assertEquals("New City", testUser.getCity());
        verify(userRepository).save(testUser);
    }

    @Test
    void toggleWishlist_shouldAddId_whenNotPresent() {
        testUser.setWishlist(new ArrayList<>());
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        userService.toggleWishlist("1", "prod1");

        assertTrue(testUser.getWishlist().contains("prod1"));
        verify(userRepository).save(testUser);
    }

    @Test
    void toggleWishlist_shouldRemoveId_whenPresent() {
        testUser.setWishlist(new ArrayList<>());
        testUser.getWishlist().add("prod1");
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        userService.toggleWishlist("1", "prod1");

        assertFalse(testUser.getWishlist().contains("prod1"));
        verify(userRepository).save(testUser);
    }
}
