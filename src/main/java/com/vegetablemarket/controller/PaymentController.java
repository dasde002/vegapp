package com.vegetablemarket.controller;

import com.vegetablemarket.dto.PaymentResponse;
import com.vegetablemarket.dto.RazorpayOrderResponse;
import com.vegetablemarket.dto.RazorpayVerifyRequest;
import com.vegetablemarket.service.PaymentService;
import com.vegetablemarket.service.RazorpayService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayService razorpayService;

    public PaymentController(PaymentService paymentService,
                             RazorpayService razorpayService) {
        this.paymentService = paymentService;
        this.razorpayService = razorpayService;
    }

    @PostMapping("/orders/{orderId}/mock")
    public PaymentResponse createMockPayment(
            @PathVariable Long orderId,
            Authentication authentication) {
        return paymentService.createMockPayment(
                authentication.getName(), orderId);
    }

    @PostMapping("/orders/{orderId}/razorpay")
    public RazorpayOrderResponse createRazorpayOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        return razorpayService.createOrder(authentication.getName(), orderId);
    }

    @PostMapping("/razorpay/verify")
    public PaymentResponse verifyRazorpayPayment(
            @Valid @RequestBody RazorpayVerifyRequest request,
            Authentication authentication) {
        return razorpayService.verifyPayment(authentication.getName(), request);
    }

    @GetMapping("/orders/{orderId}")
    public PaymentResponse getPayment(
            @PathVariable Long orderId,
            Authentication authentication) {
        return paymentService.getPayment(
                authentication.getName(), orderId);
    }
}
