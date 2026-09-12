package com.vegetablemarket.repository;

import com.vegetablemarket.entity.Order;
import com.vegetablemarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.product p
            WHERE o.user = :user
            ORDER BY o.createdAt DESC
            """)
    List<Order> findByUserWithItems(@Param("user") User user);

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.product p
            LEFT JOIN FETCH o.user u
            WHERE o.id = :orderId
            """)
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN FETCH o.items oi
            JOIN FETCH oi.product p
            JOIN FETCH o.user u
            WHERE p.sellerId = :sellerId
            ORDER BY o.createdAt DESC
            """)
    List<Order> findOrdersBySellerId(@Param("sellerId") Long sellerId);
}
