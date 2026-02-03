package com.buy01.orderservice.service.payment;

import java.util.Map;

public interface PaymentStrategy {
    Map<String, String> process(com.buy01.orderservice.model.Order order);
}
