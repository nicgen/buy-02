package com.buy01.orderservice.service.payment;

import com.buy01.orderservice.model.Order;
import com.buy01.orderservice.model.OrderItem;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeStrategyTest {

    @InjectMocks
    private StripeStrategy stripeStrategy;

    @Test
    void processShouldReturnStripeUrl() {
        // Set private fields
        ReflectionTestUtils.setField(stripeStrategy, "stripeSecretKey", "sk_test_mock_key");
        ReflectionTestUtils.setField(stripeStrategy, "domainName", "localhost");

        Order order = new Order();
        order.setId("order1");
        order.setCustomerEmail("test@example.com");

        OrderItem item = new OrderItem();
        item.setName("Product 1");
        item.setPrice(new BigDecimal("100.00"));
        item.setQuantity(1);
        order.setItems(Collections.singletonList(item));

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session session = mock(Session.class);
            when(session.getUrl()).thenReturn("http://stripe.com/checkout");
            when(session.getId()).thenReturn("sess_123");

            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(session);

            Map<String, String> result = stripeStrategy.process(order);

            assertNotNull(result);
            assertEquals("http://stripe.com/checkout", result.get("stripeUrl"));
            assertEquals("sess_123", result.get("sessionId"));
        }
    }
}
