package com.vegetablemarket.service;

import com.vegetablemarket.entity.Product;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.ProductRepository;
import com.vegetablemarket.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ProductService productService;

    @Test
    void deleteProductUsesSoftDelete() {
        User seller = new User();
        seller.setId(4L); seller.setEmail("seller1@test.com"); seller.setRole("SELLER");
        Product product = new Product();
        product.setId(3L); product.setSellerId(4L); product.setActive(true);
        when(userRepository.findByEmail("seller1@test.com")).thenReturn(Optional.of(seller));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));

        productService.deleteProduct(3L, "seller1@test.com");

        assertFalse(Boolean.TRUE.equals(product.getActive()));
        verify(productRepository).save(product);
    }

    @Test
    void sellerCannotDeleteAnotherSellersProduct() {
        User seller = new User();
        seller.setId(4L); seller.setEmail("seller1@test.com"); seller.setRole("SELLER");
        Product product = new Product();
        product.setId(3L); product.setSellerId(99L); product.setActive(true);
        when(userRepository.findByEmail("seller1@test.com")).thenReturn(Optional.of(seller));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () -> productService.deleteProduct(3L, "seller1@test.com"));
    }
}
