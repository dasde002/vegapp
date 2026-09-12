package com.vegetablemarket.controller;

import com.vegetablemarket.dto.AdminUserResponse;
import com.vegetablemarket.dto.ProductResponse;
import com.vegetablemarket.entity.Product;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.ProductRepository;
import com.vegetablemarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getUsers(Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(u -> new AdminUserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole()))
                .toList());
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts(Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(productRepository.findAll().stream().map(this::toProductResponse).toList());
    }

    @PutMapping("/products/{id}/activate")
    public ResponseEntity<ProductResponse> activateProduct(@PathVariable Long id, Authentication authentication) {
        requireAdmin(authentication);
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(toProductResponse(productRepository.save(product)));
    }

    @PutMapping("/products/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivateProduct(@PathVariable Long id, Authentication authentication) {
        requireAdmin(authentication);
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(toProductResponse(productRepository.save(product)));
    }

    private void requireAdmin(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) throw new RuntimeException("Only admins can access this operation");
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getCategory(), product.getStockQuantity(), product.getImageUrl());
    }
}
