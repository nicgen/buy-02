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
    private static final String USER_ID = "1";

    private static final String CLIENT_ROLE = "CLIENT";
    private static final String NEW_CITY = "New City";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail(EMAIL);
        testUser.setPassword(ENCODED_PASSWORD);
        testUser.setRole(CLIENT_ROLE);
        testUser.setWishlist(new ArrayList<>());
    }

    @Test
    void registerShouldCreateUserAndReturnToken() {
        AuthRequest request = new AuthRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setRole(CLIENT_ROLE);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyString(), anyString())).thenReturn(MOCK_TOKEN);

        AuthResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(MOCK_TOKEN, response.getToken());
        verify(userRepository).save(any(User.class));
    }

    // ... (skipping some methods unchanged) ...

    @Test
    void updateUserShouldUpdateAvailableFields() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setCity(NEW_CITY);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));

        userService.updateUser(EMAIL, updateRequest);

        assertEquals(NEW_CITY, testUser.getCity());
        verify(userRepository).save(testUser);
    }

    @Test
    void getProfileShouldReturnUserProfile() {
        testUser.setStreet("123 Main St");
        testUser.setCity("Metropolis");
        testUser.setZip("12345");
        testUser.setCountry("USA");
        testUser.setPhoneNumber("555-1234");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));

        com.buy01.userservice.dto.UserProfileResponse response = userService.getProfile(EMAIL);

        assertNotNull(response);
        assertEquals(USER_ID, response.getId());
        assertEquals(EMAIL, response.getEmail());
        assertEquals(CLIENT_ROLE, response.getRole());
        assertEquals("123 Main St", response.getStreet());
        assertEquals("Metropolis", response.getCity());
        assertEquals("12345", response.getZip());
        assertEquals("USA", response.getCountry());
        assertEquals("555-1234", response.getPhoneNumber());
    }

    @Test
    void getProfileShouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> userService.getProfile(EMAIL));
    }

    // ...

    @Test
    void getWishlistShouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> userService.getWishlist(USER_ID));
    }

    @Test
    void updateUserShouldUpdateAllFields() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setPassword("newPass");
        updateRequest.setStreet("New Street");
        updateRequest.setCity(NEW_CITY);
        updateRequest.setZip("54321");
        updateRequest.setCountry("Canada");
        updateRequest.setPhoneNumber("555-9876");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        userService.updateUser(EMAIL, updateRequest);

        assertEquals("encodedNewPass", testUser.getPassword());
        assertEquals("New Street", testUser.getStreet());
        assertEquals(NEW_CITY, testUser.getCity());
        assertEquals("54321", testUser.getZip());
        assertEquals("Canada", testUser.getCountry());
        assertEquals("555-9876", testUser.getPhoneNumber());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserShouldThrowExceptionWhenUserNotFound() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> userService.updateUser(EMAIL, updateRequest));
    }
}
