package com.buy01.userservice.service;

import com.buy01.userservice.dto.AuthRequest;
import com.buy01.userservice.dto.AuthResponse;
import com.buy01.userservice.model.User;
import com.buy01.userservice.repository.UserRepository;
import com.buy01.userservice.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole());
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        return new AuthResponse(token, user.getRole(), user.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        return new AuthResponse(token, user.getRole(), user.getEmail());
    }

    public void updateUser(String email, com.buy01.userservice.dto.UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getStreet() != null)
            user.setStreet(request.getStreet());
        if (request.getCity() != null)
            user.setCity(request.getCity());
        if (request.getZip() != null)
            user.setZip(request.getZip());
        if (request.getCountry() != null)
            user.setCountry(request.getCountry());
        if (request.getPhoneNumber() != null)
            user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);
    }

    public com.buy01.userservice.dto.UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new com.buy01.userservice.dto.UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStreet(),
                user.getCity(),
                user.getZip(),
                user.getCountry(),
                user.getPhoneNumber());
    }

    public void toggleWishlist(String userId, String productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getWishlist().contains(productId)) {
            user.getWishlist().remove(productId);
        } else {
            user.getWishlist().add(productId);
        }
        userRepository.save(user);
    }

    public java.util.List<String> getWishlist(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getWishlist();
    }
}
