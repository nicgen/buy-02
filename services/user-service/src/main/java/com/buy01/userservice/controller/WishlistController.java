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
        String actualToken = token.substring(7);
        String userId = jwtUtil.extractUserId(actualToken);

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        userService.toggleWishlist(userId, productId);
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
