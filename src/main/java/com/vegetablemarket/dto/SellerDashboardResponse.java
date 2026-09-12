package com.vegetablemarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SellerDashboardResponse {
    private long totalProducts;
    private long lowStockProducts;
    private long totalOrders;
    private long placedOrders;
    private long confirmedOrders;
    private long shippedOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private double totalSales;
}
