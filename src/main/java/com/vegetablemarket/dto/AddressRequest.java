package com.vegetablemarket.dto;

import lombok.Data;

@Data
public class AddressRequest {

    private String fullName;
    private String phone;
    private String addressLine;
    private String city;
    private String state;
    private String postalCode;
    private String landmark;
    private Boolean defaultAddress;
}
