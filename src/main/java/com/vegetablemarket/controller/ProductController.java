package com.vegetablemarket.controller;

import com.vegetablemarket.dto.ProductRequest;
import com.vegetablemarket.dto.ProductResponse;
import com.vegetablemarket.dto.ProductSearchResponse;
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
     * Seller creates product.
     */
    @PostMapping
    public ProductResponse addProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        return productService.addProduct(
                request,
                authentication.getName());
    }

    /**
     * Customers and sellers can view all products.
     */
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Search/filter/paginate products.
     * Example:
     * /api/products/search?keyword=potato&category=Vegetables&minPrice=10&maxPrice=100&inStock=true&page=0&size=20&sortBy=price&direction=asc
     */
    @GetMapping("/search")
    public ProductSearchResponse searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return productService.searchProducts(
                keyword,
                category,
                minPrice,
                maxPrice,
                inStock,
                page,
                size,
                sortBy,
                direction);
    }

    /**
     * Seller views their own products.
     */
    @GetMapping("/seller/my-products")
    public List<ProductResponse> getMyProducts(
            Authentication authentication) {

        return productService.getMyProducts(authentication.getName());
    }

    /**
     * Get single product.
     */
    @GetMapping("/{id}")
    public ProductResponse getProductById(
            @PathVariable Long id) {

        return productService.getProductById(id);
    }

    /**
     * Seller updates their own product.
     */
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {

        return productService.updateProduct(
                id,
                request,
                authentication.getName());
    }

    /**
     * Seller deletes their own product.
     */
    @DeleteMapping("/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {

        productService.deleteProduct(id, authentication.getName());
        return "Product deleted successfully";
    }
}
