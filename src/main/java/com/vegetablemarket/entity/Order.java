package com.vegetablemarket.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Shipping address snapshot.
    // These fields intentionally remain nullable so existing orders
    // already stored in the database continue to work.
    private String shippingFullName;
    private String shippingPhone;

    @Column(length = 500)
    private String shippingAddressLine;

    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingLandmark;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference
    private List<OrderItem> items;
}
