package com.buy01.orderservice.controller;

import com.buy01.orderservice.model.Order;
import com.buy01.orderservice.model.OrderStatus;
import com.buy01.orderservice.security.JwtUtil;
import com.buy01.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Order order;
    private final String token = "Bearer mockToken";

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId("1");
        order.setUserId("user1");
        order.setCustomerEmail("user@example.com");
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void createOrderShouldReturnCreatedOrder() throws Exception {
        given(jwtUtil.extractUserId("mockToken")).willReturn("user1");
        given(jwtUtil.extractUsername("mockToken")).willReturn("user@example.com");
        given(orderService.createOrder(any(Order.class))).willReturn(order);

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    @WithMockUser
    void getMyOrdersShouldReturnList() throws Exception {
        given(jwtUtil.extractUserId("mockToken")).willReturn("user1");
        given(orderService.getOrdersByUserId("user1")).willReturn(Arrays.asList(order));

        mockMvc.perform(get("/api/orders")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user1"));
    }

    @Test
    @WithMockUser
    void getOrdersByUserIdShouldReturnList() throws Exception {
        given(orderService.getOrdersByUserId("user1")).willReturn(Arrays.asList(order));

        mockMvc.perform(get("/api/orders/user/{userId}", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user1"));
    }

    @Test
    @WithMockUser
    void getAllOrdersShouldReturnList() throws Exception {
        given(orderService.getAllOrders()).willReturn(Arrays.asList(order));

        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    @WithMockUser
    void updateStatusShouldReturnUpdatedOrder() throws Exception {
        order.setStatus(OrderStatus.DELIVERED);
        given(orderService.updateOrderStatus("1", OrderStatus.DELIVERED)).willReturn(order);

        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "DELIVERED");

        mockMvc.perform(patch("/api/orders/{id}/status", "1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    @WithMockUser
    void getUserStatsShouldReturnMap() throws Exception {
        Map<String, Object> stats = Collections.singletonMap("total", 100);
        given(orderService.getUserStats("user1")).willReturn(stats);

        mockMvc.perform(get("/api/orders/stats/user/{userId}", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100));
    }

    @Test
    @WithMockUser
    void getSellerStatsShouldReturnMap() throws Exception {
        Map<String, Object> stats = Collections.singletonMap("sales", 500);
        given(orderService.getSellerStats("seller1")).willReturn(stats);

        mockMvc.perform(get("/api/orders/stats/seller/{sellerId}", "seller1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sales").value(500));
    }
}
