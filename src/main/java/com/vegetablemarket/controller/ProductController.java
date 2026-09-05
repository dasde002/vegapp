package com.vegetablemarket.controller;

import com.vegetablemarket.dto.ProductRequest;
import com.vegetablemarket.dto.ProductResponse;
import com.vegetablemarket.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;


    /**
     * Seller creates product
     */
    @PostMapping
    public ProductResponse addProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        return productService.addProduct(request, email);
    }


    /**
     * Customers and sellers can view all products
     */
    @GetMapping
    public List<ProductResponse> getAllProducts() {

        return productService.getAllProducts();
    }


    /**
     * Get single product
     */
    @GetMapping("/{id}")
    public ProductResponse getProductById(
            @PathVariable Long id) {

        return productService.getProductById(id);
    }


    /**
     * Seller views their own products
     *
     * GET /api/products/my-products
     */
    @GetMapping("/my-products")
    public List<ProductResponse> getMyProducts(
            Authentication authentication) {

            String email = authentication.getName();

        return productService.getMyProducts(email);
    }


    /**
     * Seller updates their own product
     */
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        return productService.updateProduct(
                id,
                request,
                email
        );
    }


    /**
     * Seller deletes their own product
     */
    @DeleteMapping("/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        productService.deleteProduct(id, email);

        return "Product deleted successfully";
    }
}

