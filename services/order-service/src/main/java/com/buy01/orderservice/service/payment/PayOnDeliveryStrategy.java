package com.buy01.orderservice.service.payment;

import java.util.Map;
import org.springframework.stereotype.Component;
import com.buy01.orderservice.model.Order;

@Component("PAY_ON_DELIVERY")
public class PayOnDeliveryStrategy implements PaymentStrategy {

    @Override
    public Map<String, String> process(Order order) {
        // No processing needed for Pay On Delivery
        return new java.util.HashMap<>();
    }
}
