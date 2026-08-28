package com.vegetablemarket.repository;

import com.vegetablemarket.entity.Order;
import com.vegetablemarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}
