package com.vegetablemarket.controller;

import com.vegetablemarket.dto.SellerOrderResponse;
import com.vegetablemarket.entity.OrderStatus;
import com.vegetablemarket.service.OrderService;

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


    // GET ALL ORDERS CONTAINING SELLER PRODUCTS
    @GetMapping
    public ResponseEntity<List<SellerOrderResponse>> getSellerOrders(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                orderService.getSellerOrders(email)
        );
    }


    // GET ONE SELLER ORDER
    @GetMapping("/{orderId}")
    public ResponseEntity<SellerOrderResponse> getSellerOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                orderService.getSellerOrder(
                        email,
                        orderId
                )
        );
    }


    // UPDATE ORDER STATUS
    @PutMapping("/{orderId}/status")
    public ResponseEntity<SellerOrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                orderService.updateSellerOrderStatus(
                        email,
                        orderId,
                        status
                )
        );
    }
}


