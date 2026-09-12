package com.vegetablemarket.controller;

import com.vegetablemarket.dto.PaymentResponse;
import com.vegetablemarket.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}/mock")
    public PaymentResponse createMockPayment(
            @PathVariable Long orderId,
            Authentication authentication) {
        return paymentService.createMockPayment(
                authentication.getName(), orderId);
    }

    @GetMapping("/orders/{orderId}")
    public PaymentResponse getPayment(
            @PathVariable Long orderId,
            Authentication authentication) {
        return paymentService.getPayment(
                authentication.getName(), orderId);
    }
}
