package com.vegetablemarket.service;

public interface PaymentGateway {
    PaymentGatewayResult createPayment(Long orderId, Double amount);
}
