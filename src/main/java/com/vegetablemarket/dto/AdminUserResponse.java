package com.vegetablemarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
}
