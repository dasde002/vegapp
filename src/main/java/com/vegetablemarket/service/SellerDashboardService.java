package com.vegetablemarket.service;

import com.vegetablemarket.dto.SellerDashboardResponse;
import com.vegetablemarket.entity.OrderItemStatus;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.entity.Order;
import com.vegetablemarket.entity.OrderItem;
import com.vegetablemarket.repository.OrderRepository;
import com.vegetablemarket.repository.ProductRepository;
import com.vegetablemarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerDashboardService {

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    public SellerDashboardResponse getDashboard(String email) {
        User seller = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (!"SELLER".equalsIgnoreCase(seller.getRole())) throw new RuntimeException("Only sellers can access the dashboard");

        long totalProducts = productRepository.findBySellerIdAndActiveTrue(seller.getId()).size();
        long lowStockProducts = productRepository.findBySellerIdAndActiveTrue(seller.getId()).stream()
                .filter(p -> p.getStockQuantity() != null && p.getStockQuantity() <= 10)
                .count();

        List<Order> orders = orderRepository.findOrdersBySellerId(seller.getId());
        long placed = 0, confirmed = 0, shipped = 0, delivered = 0, cancelled = 0;
        double totalSales = 0.0;

        for (Order order : orders) {
            List<OrderItem> sellerItems = order.getItems().stream()
                    .filter(item -> seller.getId().equals(item.getProduct().getSellerId()))
                    .toList();
            for (OrderItem item : sellerItems) {
                OrderItemStatus status = item.getStatus();
                if (status == null) status = switch (order.getStatus()) {
                    case PLACED -> OrderItemStatus.PLACED;
                    case CONFIRMED -> OrderItemStatus.CONFIRMED;
                    case SHIPPED -> OrderItemStatus.SHIPPED;
                    case DELIVERED -> OrderItemStatus.DELIVERED;
                    case CANCELLED -> OrderItemStatus.CANCELLED;
                };
                switch (status) {
                    case PLACED -> placed++;
                    case CONFIRMED -> confirmed++;
                    case SHIPPED -> shipped++;
                    case DELIVERED -> { delivered++; totalSales += item.getPrice() * item.getQuantity(); }
                    case CANCELLED -> cancelled++;
                }
            }
        }

        return new SellerDashboardResponse(
                totalProducts,
                lowStockProducts,
                orders.size(),
                placed,
                confirmed,
                shipped,
                delivered,
                cancelled,
                totalSales
        );
    }
}
