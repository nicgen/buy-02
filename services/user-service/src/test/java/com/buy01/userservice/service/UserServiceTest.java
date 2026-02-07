package com.buy01.userservice.service;

import com.buy01.userservice.dto.AuthRequest;
import com.buy01.userservice.dto.AuthResponse;
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
import static org.mockito.ArgumentMatchers.anyString;
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

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String MOCK_TOKEN = "mockToken";
    private static final String PRODUCT_ID = "prod1";
    private static final String USER_ID = "1";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail(EMAIL);
        testUser.setPassword(ENCODED_PASSWORD);
        testUser.setRole("CLIENT");
        testUser.setWishlist(new ArrayList<>());
    }

    @Test
    void registerShouldCreateUserAndReturnToken() {
        AuthRequest request = new AuthRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setRole("CLIENT");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyString(), anyString())).thenReturn(MOCK_TOKEN);

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(MOCK_TOKEN, response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsValid() {
        AuthRequest request = new AuthRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getEmail(), testUser.getRole(), testUser.getId())).thenReturn(MOCK_TOKEN);

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals(MOCK_TOKEN, response.getToken());
    }

    @Test
    void updateUserShouldUpdateAvailableFields() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setCity("New City");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));

        userService.updateUser(EMAIL, updateRequest);

        assertEquals("New City", testUser.getCity());
        verify(userRepository).save(testUser);
    }

    @Test
    void toggleWishlistShouldAddIdWhenNotPresent() {
        testUser.setWishlist(new ArrayList<>());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        userService.toggleWishlist(USER_ID, PRODUCT_ID);

        assertTrue(testUser.getWishlist().contains(PRODUCT_ID));
        verify(userRepository).save(testUser);
    }

    @Test
    void toggleWishlistShouldRemoveIdWhenPresent() {
        testUser.setWishlist(new ArrayList<>());
        testUser.getWishlist().add(PRODUCT_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        userService.toggleWishlist(USER_ID, PRODUCT_ID);

        assertFalse(testUser.getWishlist().contains(PRODUCT_ID));
        verify(userRepository).save(testUser);
    }
}
