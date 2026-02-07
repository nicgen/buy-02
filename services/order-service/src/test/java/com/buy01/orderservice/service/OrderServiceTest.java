package com.buy01.orderservice.service;

import com.buy01.orderservice.model.Order;
import com.buy01.orderservice.model.OrderItem;
import com.buy01.orderservice.model.OrderStatus;
import com.buy01.orderservice.repository.OrderRepository;
import com.buy01.orderservice.service.payment.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentStrategy mockPaymentStrategy;

    private OrderService orderService;

    private Order testOrder;

    private static final String MOCK_PAYMENT = "MOCK_PAYMENT";
    private static final String USER_ID = "user1";
    private static final String SELLER_ID = "seller1";

    @BeforeEach
    void setUp() {
        Map<String, PaymentStrategy> paymentStrategies = new HashMap<>();
        paymentStrategies.put(MOCK_PAYMENT, mockPaymentStrategy);
        orderService = new OrderService(orderRepository, paymentStrategies);

        testOrder = new Order();
        testOrder.setId("1");
        testOrder.setUserId(USER_ID);
        testOrder.setTotalAmount(BigDecimal.valueOf(100.00));
        testOrder.setPaymentMethod(MOCK_PAYMENT);
        testOrder.setStatus(OrderStatus.PENDING);
    }

    @Test
    void createOrderShouldUseCorrectStrategyAndSave() {
        // Setup mock strategy behavior
        Map<String, String> paymentDetails = new HashMap<>();
        paymentDetails.put("transactionId", "txn_123");
        when(mockPaymentStrategy.process(any(Order.class))).thenReturn(paymentDetails);

        // Setup repo behavior
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrder(testOrder);

        assertNotNull(result);
        verify(mockPaymentStrategy).process(any(Order.class));
        verify(orderRepository, times(2)).save(any(Order.class)); // Once initially, once after payment
        assertEquals("txn_123", result.getPaymentDetails().get("transactionId"));
    }

    @Test
    void getUserStatsShouldCalculateCorrectly() {
        Order order1 = new Order();
        order1.setTotalAmount(BigDecimal.valueOf(50.00));
        order1.setStatus(OrderStatus.DELIVERED);

        Order order2 = new Order();
        order2.setTotalAmount(BigDecimal.valueOf(50.00));
        order2.setStatus(OrderStatus.PENDING);

        List<Order> orders = List.of(order1, order2);
        when(orderRepository.findByUserId(USER_ID)).thenReturn(orders);

        Map<String, Object> stats = orderService.getUserStats(USER_ID);

        assertEquals(BigDecimal.valueOf(100.00), stats.get("totalSpent"));
        assertEquals(1L, stats.get("completedOrders"));
        assertEquals(2, stats.get("totalOrders"));
    }

    @Test
    void getSellerStatsShouldCalculateSalesForSellerOnly() {
        // Order with an item from seller1
        OrderItem item1 = new OrderItem();
        item1.setSellerId(SELLER_ID);
        item1.setPrice(BigDecimal.valueOf(10.00));
        item1.setQuantity(2); // Total 20.00

        // Order with an item from seller2
        OrderItem item2 = new OrderItem();
        item2.setSellerId("seller2");
        item2.setPrice(BigDecimal.valueOf(5.00));
        item2.setQuantity(1);

        Order order = new Order();
        order.setStatus(OrderStatus.DELIVERED);
        order.setItems(List.of(item1, item2));

        when(orderRepository.findAll()).thenReturn(Collections.singletonList(order));

        Map<String, Object> stats = orderService.getSellerStats(SELLER_ID);

        assertEquals(BigDecimal.valueOf(20.00), stats.get("totalSales"));
        assertEquals(2, stats.get("totalItemsSold"));
    }

    @Test
    void getOrdersByUserIdShouldReturnOrders() {
        when(orderRepository.findByUserId(USER_ID)).thenReturn(Collections.singletonList(testOrder));

        List<Order> result = orderService.getOrdersByUserId(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(USER_ID, result.get(0).getUserId());
    }

    @Test
    void getAllOrdersShouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(testOrder));

        List<Order> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateOrderStatusShouldUpdateAndSave() {
        when(orderRepository.findById("1")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus("1", OrderStatus.DELIVERED);

        assertEquals(OrderStatus.DELIVERED, result.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatusShouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus("999", OrderStatus.DELIVERED);
        });
    }
}
