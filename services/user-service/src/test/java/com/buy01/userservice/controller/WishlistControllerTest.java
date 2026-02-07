package com.buy01.userservice.controller;

import com.buy01.userservice.security.JwtUtil;
import com.buy01.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private final String token = "Bearer mockToken";

    @Test
    @WithMockUser
    void toggleWishlistShouldReturnOk() throws Exception {
        given(jwtUtil.extractUserId("mockToken")).willReturn("user1");

        mockMvc.perform(post("/api/wishlist/prod1")
                .with(csrf())
                .header("Authorization", token))
                .andExpect(status().isOk());

        verify(userService).toggleWishlist("user1", "prod1");
    }

    @Test
    @WithMockUser
    void toggleWishlistShouldReturnUnauthorizedIfUserIdNull() throws Exception {
        given(jwtUtil.extractUserId("mockToken")).willReturn(null);

        mockMvc.perform(post("/api/wishlist/prod1")
                .with(csrf())
                .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getWishlistShouldReturnList() throws Exception {
        // Mock the extractClaim to work with the functional interface
        // This is tricky with Mockito, so we often mock the behavior directly if
        // possible,
        // but here the controller calls jwtUtil.extractClaim(token, claims ->
        // claims.get("userId", String.class))
        // We need to match any string and any function
        given(jwtUtil.extractClaim(eq("mockToken"), any())).willReturn("user1");

        List<String> wishlist = Arrays.asList("prod1", "prod2");
        given(userService.getWishlist("user1")).willReturn(wishlist);

        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("prod1"))
                .andExpect(jsonPath("$[1]").value("prod2"));
    }

    @Test
    @WithMockUser
    void getWishlistShouldReturnUnauthorizedIfUserIdNull() throws Exception {
        given(jwtUtil.extractClaim(eq("mockToken"), any())).willReturn(null);

        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }
}
