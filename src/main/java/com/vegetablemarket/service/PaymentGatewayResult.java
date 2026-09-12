package com.vegetablemarket.service;

import com.vegetablemarket.entity.PaymentStatus;

public record PaymentGatewayResult(
        PaymentStatus status,
        String transactionId) {
}
