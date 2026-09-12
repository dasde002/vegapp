package com.vegetablemarket.controller;

import com.vegetablemarket.dto.SellerDashboardResponse;
import com.vegetablemarket.service.SellerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/dashboard")
public class SellerDashboardController {

    @Autowired
    private SellerDashboardService sellerDashboardService;

    @GetMapping
    public ResponseEntity<SellerDashboardResponse> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(sellerDashboardService.getDashboard(authentication.getName()));
    }
}
