package com.vegetablemarket.repository;

import com.vegetablemarket.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerIdAndActiveTrue(Long sellerId);

    List<Product> findByActiveTrue();

    @Modifying
    @Transactional
    @Query(value = "UPDATE products SET active = COALESCE(active, true), version = COALESCE(version, 0)", nativeQuery = true)
    int initializeLifecycleColumns();

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.active = true
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR :category = ''
                   OR LOWER(p.category) = LOWER(:category))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStock IS NULL OR :inStock = false OR p.stockQuantity > 0)
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("inStock") Boolean inStock,
            Pageable pageable);
}
