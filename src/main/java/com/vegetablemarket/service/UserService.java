package com.vegetablemarket.service;

import com.vegetablemarket.dto.ChangePasswordRequest;
import com.vegetablemarket.dto.LoginRequest;
import com.vegetablemarket.dto.LoginResponse;
import com.vegetablemarket.dto.RegisterRequest;
import com.vegetablemarket.dto.UpdateProfileRequest;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Public registration always creates a CUSTOMER.
     * Seller accounts must be created through a trusted/admin process.
     */
    public User registerUser(RegisterRequest request) {

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new RuntimeException("Phone number is required");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must contain at least 6 characters");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Never trust the role supplied by an unauthenticated client.
        user.setRole("CUSTOMER");

        return userRepository.save(user);
    }

    /**
     * Login user and generate JWT token.
     */
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                token,
                "Login Successful"
        );
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = getCurrentUser(email);

        if (userRepository.findByPhone(request.getPhone())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone().trim());

        return userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getCurrentUser(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
