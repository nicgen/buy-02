package com.buy01.orderservice.service;

import com.buy01.orderservice.model.Order;
import com.buy01.orderservice.model.OrderItem;
import com.buy01.orderservice.model.OrderStatus;
import com.buy01.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrderService {

    private final com.buy01.orderservice.service.payment.PaymentStrategy defaultStrategy = new com.buy01.orderservice.service.payment.PayOnDeliveryStrategy();
    private final Map<String, com.buy01.orderservice.service.payment.PaymentStrategy> paymentStrategies;
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository,
            Map<String, com.buy01.orderservice.service.payment.PaymentStrategy> paymentStrategies) {
        this.orderRepository = orderRepository;
        this.paymentStrategies = paymentStrategies;
    }

    public Order createOrder(Order order) {
        // Save first to generate ID
        Order savedOrder = orderRepository.save(order);

        String method = savedOrder.getPaymentMethod();
        if (method == null)
            method = "PAY_ON_DELIVERY";

        com.buy01.orderservice.service.payment.PaymentStrategy strategy = paymentStrategies.get(method);
        if (strategy == null)
            strategy = defaultStrategy;

        Map<String, String> paymentResult = strategy.process(savedOrder);
        if (savedOrder.getPaymentDetails() == null) {
            savedOrder.setPaymentDetails(new HashMap<>());
        }
        savedOrder.getPaymentDetails().putAll(paymentResult);

        return orderRepository.save(savedOrder);
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Map<String, Object> getUserStats(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSpent", totalSpent);
        stats.put("completedOrders", completedOrders);
        stats.put("totalOrders", orders.size());
        return stats;
    }

    public Map<String, Object> getSellerStats(String sellerId) {
        List<Order> allOrders = orderRepository.findAll();
        BigDecimal totalSales = BigDecimal.ZERO;
        int totalItemsSold = 0;

        for (Order order : allOrders) {
            // Only count if order is not cancelled
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }

            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (sellerId.equals(item.getSellerId())) {
                        BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        totalSales = totalSales.add(itemTotal);
                        totalItemsSold += item.getQuantity();
                    }
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSales", totalSales);
        stats.put("totalItemsSold", totalItemsSold);
        return stats;
    }
}
