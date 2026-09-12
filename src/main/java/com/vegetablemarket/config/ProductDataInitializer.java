package com.vegetablemarket.config;

import com.vegetablemarket.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class ProductDataInitializer {

    @Bean
    CommandLineRunner initializeProductLifecycleData(ProductRepository productRepository) {
        return args -> initialize(productRepository);
    }

    @Transactional
    void initialize(ProductRepository productRepository) {
        productRepository.initializeLifecycleColumns();
    }
}
