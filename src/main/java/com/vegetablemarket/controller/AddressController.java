package com.vegetablemarket.controller;

import com.vegetablemarket.dto.AddressRequest;
import com.vegetablemarket.entity.Address;
import com.vegetablemarket.service.AddressService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;


    // ADD ADDRESS
    @PostMapping
    public ResponseEntity<Address> addAddress(
            @RequestBody AddressRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                addressService.addAddress(
                        authentication.getName(),
                        request
                )
        );
    }


    // GET ALL ADDRESSES
    @GetMapping
    public ResponseEntity<List<Address>> getAddresses(
            Authentication authentication) {

        return ResponseEntity.ok(
                addressService.getAddresses(
                        authentication.getName()
                )
        );
    }


    // GET SINGLE ADDRESS
    @GetMapping("/{addressId}")
    public ResponseEntity<Address> getAddress(
            @PathVariable Long addressId,
            Authentication authentication) {

        return ResponseEntity.ok(
                addressService.getAddress(
                        authentication.getName(),
                        addressId
                )
        );
    }


    // UPDATE ADDRESS
    @PutMapping("/{addressId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long addressId,
            @RequestBody AddressRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                addressService.updateAddress(
                        authentication.getName(),
                        addressId,
                        request
                )
        );
    }


    // DELETE ADDRESS
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication) {

        addressService.deleteAddress(
                authentication.getName(),
                addressId
        );

        return ResponseEntity.noContent().build();
    }


    // SET DEFAULT ADDRESS
    @PutMapping("/{addressId}/default")
    public ResponseEntity<Address> setDefaultAddress(
            @PathVariable Long addressId,
            Authentication authentication) {

        return ResponseEntity.ok(
                addressService.setDefaultAddress(
                        authentication.getName(),
                        addressId
                )
        );
    }
}

