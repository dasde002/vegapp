package com.vegetablemarket.dto;

import com.vegetablemarket.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private Double amount;
    private PaymentStatus status;
    private String provider;
    private String transactionId;
    private String razorpayOrderId;
}
