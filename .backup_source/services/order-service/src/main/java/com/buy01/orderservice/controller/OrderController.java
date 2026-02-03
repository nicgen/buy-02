package com.buy01.orderservice.controller;

import com.buy01.orderservice.model.Order;
import com.buy01.orderservice.model.OrderStatus;
import com.buy01.orderservice.service.OrderService;
import com.buy01.orderservice.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OrderController.class);

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order, @RequestHeader("Authorization") String token) {
        logger.info("Received create order request");
        try {
            String tokenBody = token.substring(7);
            String userId = jwtUtil.extractUserId(tokenBody);
            String email = jwtUtil.extractUsername(tokenBody);
            logger.info("Extracted userId: {}, email: {}", userId, email);

            if (userId == null) {
                logger.error("UserId is null after extraction");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            order.setUserId(userId);
            order.setCustomerEmail(email);
            order.setStatus(OrderStatus.PENDING);
            order.setCreatedAt(java.time.LocalDateTime.now());

            Order createdOrder = orderService.createOrder(order);
            logger.info("Order created successfully with ID: {}", createdOrder.getId());
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating order", e);
            throw e; // Or return generic error
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(@RequestHeader("Authorization") String token) {
        String userId = jwtUtil.extractUserId(token.substring(7));
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable String userId) {
        // TODO: Validate that the requesting user matches the userId or is
        // Admin/Seller?
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable String id, @RequestBody Map<String, String> statusUpdate) {
        String statusStr = statusUpdate.get("status");
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr);
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getUserStats(userId));
    }

    @GetMapping("/stats/seller/{sellerId}")
    public ResponseEntity<Map<String, Object>> getSellerStats(@PathVariable String sellerId) {
        return ResponseEntity.ok(orderService.getSellerStats(sellerId));
    }
}
