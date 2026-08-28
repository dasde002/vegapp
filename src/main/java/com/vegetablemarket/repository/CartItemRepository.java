package com.vegetablemarket.repository;

import com.vegetablemarket.entity.CartItem;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);
}
