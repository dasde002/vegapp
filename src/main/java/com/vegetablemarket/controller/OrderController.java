package com.vegetablemarket.controller;

import com.vegetablemarket.entity.Order;
import com.vegetablemarket.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;


    // CHECKOUT
    @PostMapping
    public ResponseEntity<Order> checkout(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                orderService.checkout(email)
        );
    }


    // GET MY ORDERS
    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                orderService.getMyOrders(email)
        );
    }


    // GET SINGLE ORDER
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                orderService.getOrder(email, orderId)
        );
    }
    

    // CANCEL ORDER
@PutMapping("/{orderId}/cancel")
public ResponseEntity<Order> cancelOrder(
        @PathVariable Long orderId,
        Authentication authentication) {

    String email = authentication.getName();

    return ResponseEntity.ok(
            orderService.cancelOrder(email, orderId)
    );
    }

}
