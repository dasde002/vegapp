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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public ProductResponse addProduct(ProductRequest request, String email) {
        User seller = getSeller(email);
        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setCategory(request.getCategory().trim());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setSellerId(seller.getId());
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return toProductResponse(productRepository.save(product));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findByActiveTrue().stream().map(this::toProductResponse).collect(Collectors.toList());
    }

    public ProductSearchResponse searchProducts(String keyword, String category, Double minPrice, Double maxPrice,
                                                Boolean inStock, int page, int size, String sortBy, String direction) {
        if (page < 0) throw new RuntimeException("Page must be zero or greater");
        if (size < 1 || size > 100) throw new RuntimeException("Size must be between 1 and 100");
        if (minPrice != null && minPrice < 0) throw new RuntimeException("Minimum price cannot be negative");
        if (maxPrice != null && maxPrice < 0) throw new RuntimeException("Maximum price cannot be negative");
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) throw new RuntimeException("Minimum price cannot exceed maximum price");
        String safeSortBy = switch (sortBy == null ? "" : sortBy) {
            case "price", "name", "createdAt", "updatedAt" -> sortBy;
            default -> "createdAt";
        };
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, safeSortBy));
        Page<Product> result = productRepository.searchProducts(normalize(keyword), normalize(category), minPrice, maxPrice, inStock, pageable);
        List<ProductResponse> products = result.getContent().stream().map(this::toProductResponse).collect(Collectors.toList());
        return new ProductSearchResponse(products, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id).filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toProductResponse(product);
    }

    public List<ProductResponse> getMyProducts(String email) {
        User seller = getSeller(email);
        return productRepository.findBySellerIdAndActiveTrue(seller.getId()).stream().map(this::toProductResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String email) {
        User seller = getSeller(email);
        Product product = getOwnedProduct(id, seller.getId());
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setCategory(request.getCategory().trim());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setUpdatedAt(LocalDateTime.now());
        return toProductResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateInventory(Long id, Integer quantity, String email) {
        User seller = getSeller(email);
        Product product = getOwnedProduct(id, seller.getId());
        product.setStockQuantity(quantity);
        product.setUpdatedAt(LocalDateTime.now());
        return toProductResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id, String email) {
        User seller = getSeller(email);
        Product product = getOwnedProduct(id, seller.getId());
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private User getSeller(String email) {
        User seller = getUserByEmail(email);
        if (!"SELLER".equalsIgnoreCase(seller.getRole())) throw new RuntimeException("Only sellers can access this operation");
        return seller;
    }

    private Product getOwnedProduct(Long id, Long sellerId) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        if (!Boolean.TRUE.equals(product.getActive())) throw new RuntimeException("Product is no longer active");
        if (!sellerId.equals(product.getSellerId())) throw new RuntimeException("You are not authorized to update this product");
        return product;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String normalize(String value) { return value == null ? null : value.trim(); }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getCategory(), product.getStockQuantity(), product.getImageUrl());
    }
}
