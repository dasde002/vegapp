package com.vegetablemarket.service;

import com.vegetablemarket.dto.ProductRequest;
import com.vegetablemarket.dto.ProductResponse;
import com.vegetablemarket.entity.Product;
import com.vegetablemarket.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;


public ProductResponse addProduct(ProductRequest request) {

    Product product = new Product();

    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setCategory(request.getCategory());
    product.setPrice(request.getPrice());

    product.setStockQuantity(request.getQuantity());

    product.setImageUrl(request.getImageUrl());

    product.setSellerId(1L);

    Product savedProduct = productRepository.save(product);

    return new ProductResponse(
            savedProduct.getId(),
            savedProduct.getName(),
            savedProduct.getDescription(),
            savedProduct.getPrice(),
            savedProduct.getCategory(),
            savedProduct.getStockQuantity(),
            savedProduct.getImageUrl()
    );
}


public List<ProductResponse> getAllProducts() {

    List<Product> products = productRepository.findAll();

    return products.stream()
            .map(product -> new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getCategory(),
                    product.getStockQuantity(),
                    product.getImageUrl()
            ))
            .collect(Collectors.toList());
}


public ProductResponse getProductById(Long id) {

    Product product = productRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Product not found"));

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

public ProductResponse updateProduct(Long id,
                                     ProductRequest request) {

    Product product = productRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Product not found"));

    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setCategory(request.getCategory());
    product.setPrice(request.getPrice());
    product.setStockQuantity(request.getQuantity());
    product.setImageUrl(request.getImageUrl());

    Product updatedProduct = productRepository.save(product);

    return new ProductResponse(
            updatedProduct.getId(),
            updatedProduct.getName(),
            updatedProduct.getDescription(),
            updatedProduct.getPrice(),
            updatedProduct.getCategory(),
            updatedProduct.getStockQuantity(),
            updatedProduct.getImageUrl()
    );
}


public void deleteProduct(Long id) {

    if (!productRepository.existsById(id)) {
        throw new RuntimeException("Product not found");
    }

    productRepository.deleteById(id);
}

}
