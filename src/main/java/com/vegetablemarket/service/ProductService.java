package com.vegetablemarket.service;

import com.vegetablemarket.dto.ProductRequest;
import com.vegetablemarket.dto.ProductResponse;
import com.vegetablemarket.entity.Product;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.ProductRepository;
import com.vegetablemarket.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * Seller creates a new product
     */
    public ProductResponse addProduct(ProductRequest request, String email) {

        User seller = getUserByEmail(email);

        if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
            throw new RuntimeException("Only sellers can add products");
        }

        Product product = new Product();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());

        // IMPORTANT:
        // Seller ID comes from authenticated user,
        // NOT from the request.
        product.setSellerId(seller.getId());

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        return toProductResponse(savedProduct);
    }


    /**
     * Customers and sellers can view all products
     */
    public List<ProductResponse> getAllProducts() {

        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }


    /**
     * Get single product
     */
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        return toProductResponse(product);
    }


    /**
     * Seller views only their own products
     */
    public List<ProductResponse> getMyProducts(String email) {

        User seller = getUserByEmail(email);

        if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
            throw new RuntimeException("Only sellers can view their products");
        }

        return productRepository.findBySellerId(seller.getId())
                .stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }


    /**
     * Seller updates their own product
     */
    public ProductResponse updateProduct(
            Long id,
            ProductRequest request,
            String email) {

        User seller = getUserByEmail(email);

        if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
            throw new RuntimeException("Only sellers can update products");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        // Ownership check
        if (!product.getSellerId().equals(seller.getId())) {
            throw new RuntimeException(
                    "You are not authorized to update this product");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());

        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        return toProductResponse(updatedProduct);
    }


    /**
     * Seller deletes their own product
     */
    public void deleteProduct(Long id, String email) {

        User seller = getUserByEmail(email);

        if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
            throw new RuntimeException("Only sellers can delete products");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        // Ownership check
        if (!product.getSellerId().equals(seller.getId())) {
            throw new RuntimeException(
                    "You are not authorized to delete this product");
        }

        productRepository.delete(product);
    }


    /**
     * Find user by email
     */
    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }


    /**
     * Convert Product entity to ProductResponse
     */
    private ProductResponse toProductResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStockQuantity(),
                product.getImageUrl()
        );
    }
}

