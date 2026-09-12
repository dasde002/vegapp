package com.vegetablemarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RazorpayOrderResponse {
    private Long paymentId;
    private Long orderId;
    private String razorpayOrderId;
    private Long amount;
    private String currency;
    private String keyId;
    private String status;
}
