package com.vegetablemarket.dto;

import com.vegetablemarket.entity.OrderItemStatus;
import lombok.Data;

@Data
public class SellerOrderItemResponse {

    private Long orderItemId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private OrderItemStatus status;
}
