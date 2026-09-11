package com.vegetablemarket.repository;

import com.vegetablemarket.entity.Order;
import com.vegetablemarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN o.items oi
            JOIN oi.product p
            WHERE p.sellerId = :sellerId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId);
}

