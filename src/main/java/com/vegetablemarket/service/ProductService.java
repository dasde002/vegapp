package com.vegetablemarket.service;

import com.vegetablemarket.dto.ProductRequest;
import com.vegetablemarket.dto.ProductResponse;
import com.vegetablemarket.dto.ProductSearchResponse;
import com.vegetablemarket.entity.Product;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.ProductRepository;
import com.vegetablemarket.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * Seller creates a new product.
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
        product.setSellerId(seller.getId());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return toProductResponse(productRepository.save(product));
    }

    /**
     * Customers and sellers can view all products.
     */
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search, filter and paginate products.
     */
    public ProductSearchResponse searchProducts(
            String keyword,
            String category,
            Double minPrice,
            Double maxPrice,
            Boolean inStock,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0) {
            throw new RuntimeException("Page must be zero or greater");
        }

        if (size < 1 || size > 100) {
            throw new RuntimeException("Size must be between 1 and 100");
        }

        if (minPrice != null && minPrice < 0) {
            throw new RuntimeException("Minimum price cannot be negative");
        }

        if (maxPrice != null && maxPrice < 0) {
            throw new RuntimeException("Maximum price cannot be negative");
        }

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new RuntimeException("Minimum price cannot exceed maximum price");
        }

        String safeSortBy = switch (sortBy == null ? "" : sortBy) {
            case "price", "name", "createdAt", "updatedAt" -> sortBy;
            default -> "createdAt";
        };

        Sort.Direction sortDirection =
                "asc".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, safeSortBy));

        Page<Product> result = productRepository.searchProducts(
                normalize(keyword),
                normalize(category),
                minPrice,
                maxPrice,
                inStock,
                pageable);

        List<ProductResponse> products = result.getContent()
                .stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());

        return new ProductSearchResponse(
                products,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    /**
     * Get single product.
     */
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        return toProductResponse(product);
    }

    /**
     * Seller views only their own products.
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
     * Seller updates their own product.
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

        return toProductResponse(productRepository.save(product));
    }

    /**
     * Seller deletes their own product.
     */
    public void deleteProduct(Long id, String email) {

        User seller = getUserByEmail(email);

        if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
            throw new RuntimeException("Only sellers can delete products");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (!product.getSellerId().equals(seller.getId())) {
            throw new RuntimeException(
                    "You are not authorized to delete this product");
        }

        productRepository.delete(product);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStockQuantity(),
                product.getImageUrl());
    }
}
