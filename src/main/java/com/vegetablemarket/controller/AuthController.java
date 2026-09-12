package com.vegetablemarket.controller;

import com.vegetablemarket.dto.LoginRequest;
import com.vegetablemarket.dto.LoginResponse;
import com.vegetablemarket.dto.RegisterRequest;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                userService.login(request));
    }
}
