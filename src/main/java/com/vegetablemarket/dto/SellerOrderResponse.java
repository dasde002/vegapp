package com.vegetablemarket.dto;

import com.vegetablemarket.entity.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SellerOrderResponse {

    private Long orderId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Double sellerOrderTotal;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<SellerOrderItemResponse> items;
}


