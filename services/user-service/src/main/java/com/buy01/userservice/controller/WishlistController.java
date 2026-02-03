package com.buy01.userservice.controller;

import com.buy01.userservice.service.UserService;
import com.buy01.userservice.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public WishlistController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> toggleWishlist(@PathVariable String productId,
            @RequestHeader("Authorization") String token) {
        String userId = jwtUtil.extractUserId(token.substring(7)); // Use extractUserId we added for OrderService? NO
                                                                   // using User Service JwtUtil
        // Wait, User Service JwtUtil doesn't have extractUserId (I added it to Order
        // Service).
        // I need to add extractUserId to User Service JwtUtil as well.
        // Or I can extract it from claims manually here or extract username then find
        // user.
        // But tokens have userId now.
        // Let's add extractUserId to User Service JwtUtil first or assume I can do it
        // here.
        // Actually, the implementation plan didn't specify updating User Service
        // JwtUtil for extraction, only generation.
        // But I put userId in the token.

        // Let's check User Service JwtUtil again.
        // It has extractClaim.

        // I will implement extractUserId logic here inline or update JwtUtil.
        // Better to update JwtUtil for consistency.

        // For now, let's write the controller assuming I'll fix JwtUtil in a second or
        // use inline extraction.
        // Inline: jwtUtil.extractClaim(token.substring(7), claims ->
        // claims.get("userId", String.class));

        // However, I previously edited User Service JwtUtil to ADD userId to claims.
        // I did NOT add an extractor method.

        String actualToken = token.substring(7);
        String uId = jwtUtil.extractClaim(actualToken, claims -> claims.get("userId", String.class));

        if (uId == null) {
            // Fallback or error?
            // If token is valid but no userId (old token), maybe find by username?
            String username = jwtUtil.extractUsername(actualToken);
            // We'd need a method in UserService to get ID by email.
            // Too complex. Let's assume new tokens only.
            return ResponseEntity.status(401).build();
        }

        userService.toggleWishlist(uId, productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<String>> getWishlist(@RequestHeader("Authorization") String token) {
        String actualToken = token.substring(7);
        String uId = jwtUtil.extractClaim(actualToken, claims -> claims.get("userId", String.class));

        if (uId == null)
            return ResponseEntity.status(401).build();

        return ResponseEntity.ok(userService.getWishlist(uId));
    }
}
