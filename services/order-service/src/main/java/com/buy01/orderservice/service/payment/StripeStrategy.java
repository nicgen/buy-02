package com.buy01.orderservice.service.payment;

import com.buy01.orderservice.model.Order;
import com.buy01.orderservice.model.OrderItem;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("STRIPE")
public class StripeStrategy implements PaymentStrategy {

    @Value("${STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @Value("${DOMAIN_NAME:localhost}")
    private String domainName;

    @Override
    public Map<String, String> process(Order order) {
        Stripe.apiKey = stripeSecretKey;

        String frontendUrl;
        if ("localhost".equals(domainName)) {
            frontendUrl = "http://localhost:4200";
        } else {
            frontendUrl = "https://app." + domainName;
        }

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            // Stripe expects amount in cents
            long amountInCents = item.getPrice().multiply(new BigDecimal(100)).longValue();

            lineItems.add(SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.getQuantity())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(amountInCents)
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.getName())
                                    .build())
                            .build())
                    .build());
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/orders")
                .setCancelUrl(frontendUrl + "/cart")
                .addAllLineItem(lineItems)
                .putMetadata("orderId", order.getId()) // Only works if order is saved first or we generate ID
                .setCustomerEmail(order.getCustomerEmail())
                .build();

        try {
            Session session = Session.create(params);
            Map<String, String> response = new HashMap<>();
            response.put("stripeUrl", session.getUrl());
            response.put("sessionId", session.getId());
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }
}
