package com.vegetablemarket.controller;

import com.vegetablemarket.dto.OrderStatusRequest;
import com.vegetablemarket.dto.SellerOrderResponse;
import com.vegetablemarket.entity.OrderStatus;
import com.vegetablemarket.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<SellerOrderResponse>> getSellerOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getSellerOrders(authentication.getName()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<SellerOrderResponse> getSellerOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        return ResponseEntity.ok(orderService.getSellerOrder(authentication.getName(), orderId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<SellerOrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(orderService.updateSellerOrderStatus(
                authentication.getName(), orderId, request.getStatus()));
    }
}
