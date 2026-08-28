package com.vegetablemarket.controller;

import com.vegetablemarket.dto.RegisterRequest;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.vegetablemarket.dto.LoginRequest;
import com.vegetablemarket.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        return ResponseEntity.ok(user);
    } // Closes registerUser method


    @PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @RequestBody LoginRequest request) {

    LoginResponse response = userService.login(request);

    return ResponseEntity.ok(response);
   }  
} // Closes AuthController class
