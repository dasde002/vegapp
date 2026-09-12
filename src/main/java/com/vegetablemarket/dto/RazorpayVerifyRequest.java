package com.vegetablemarket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RazorpayVerifyRequest {

    @NotBlank(message = "razorpay_order_id is required")
    private String razorpayOrderId;

    @NotBlank(message = "razorpay_payment_id is required")
    private String razorpayPaymentId;

    @NotBlank(message = "razorpay_signature is required")
    private String razorpaySignature;
}
