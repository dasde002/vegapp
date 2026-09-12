package com.vegetablemarket.controller;

import com.vegetablemarket.dto.InventoryUpdateRequest;
import com.vegetablemarket.dto.ProductRequest;
import com.vegetablemarket.dto.ProductResponse;
import com.vegetablemarket.dto.ProductSearchResponse;
import com.vegetablemarket.repository.ProductRepository;
import com.vegetablemarket.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private ProductRepository productRepository;

    @PostMapping
    public ProductResponse addProduct(@Valid @RequestBody ProductRequest request, Authentication authentication) {
        return productService.addProduct(request, authentication.getName());
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return productRepository.findActiveCategories();
    }

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
        return productService.searchProducts(keyword, category, minPrice, maxPrice, inStock, page, size, sortBy, direction);
    }

    @GetMapping("/seller/my-products")
    public List<ProductResponse> getMyProducts(Authentication authentication) {
        return productService.getMyProducts(authentication.getName());
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request, Authentication authentication) {
        return productService.updateProduct(id, request, authentication.getName());
    }

    @PutMapping("/{id}/inventory")
    public ProductResponse updateInventory(@PathVariable Long id, @Valid @RequestBody InventoryUpdateRequest request, Authentication authentication) {
        return productService.updateInventory(id, request.getQuantity(), authentication.getName());
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id, Authentication authentication) {
        productService.deleteProduct(id, authentication.getName());
        return "Product deleted successfully";
    }
}
