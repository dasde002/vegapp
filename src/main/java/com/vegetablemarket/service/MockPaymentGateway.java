package com.vegetablemarket.service;

import com.vegetablemarket.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResult createPayment(Long orderId, Double amount) {
        return new PaymentGatewayResult(
                PaymentStatus.SUCCESS,
                "MOCK-" + UUID.randomUUID());
    }
}
