package com.vegetablemarket.service;

import com.vegetablemarket.entity.Address;
import com.vegetablemarket.entity.CartItem;
import com.vegetablemarket.entity.Order;
import com.vegetablemarket.entity.OrderItem;
import com.vegetablemarket.entity.OrderItemStatus;
import com.vegetablemarket.entity.OrderStatus;
import com.vegetablemarket.entity.Product;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.AddressRepository;
import com.vegetablemarket.repository.CartItemRepository;
import com.vegetablemarket.repository.OrderRepository;
import com.vegetablemarket.repository.ProductRepository;
import com.vegetablemarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;

    @Transactional
    public Order checkout(String email, Long addressId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new RuntimeException("Address not found"));
        if (!address.getUser().getId().equals(user.getId())) throw new RuntimeException("You are not authorized to use this address");
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (!Boolean.TRUE.equals(product.getActive())) throw new RuntimeException("Product is no longer available: " + product.getName());
            if (cartItem.getQuantity() > product.getStockQuantity()) throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(0.0);
        order.setShippingFullName(address.getFullName());
        order.setShippingPhone(address.getPhone());
        order.setShippingAddressLine(address.getAddressLine());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingPostalCode(address.getPostalCode());
        order.setShippingLandmark(address.getLandmark());
        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setStatus(OrderItemStatus.PLACED);
            orderItems.add(orderItem);
            totalAmount += product.getPrice() * cartItem.getQuantity();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);
        return order;
    }

    public List<Order> getMyOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserWithItems(user);
    }

    public Order getOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findByIdWithItems(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) throw new RuntimeException("Unauthorized access to order");
        return order;
    }

    @Transactional
    public Order cancelOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findByIdWithItems(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) throw new RuntimeException("Unauthorized access to order");
        if (order.getStatus() == OrderStatus.CANCELLED) throw new RuntimeException("Order is already cancelled");
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) throw new RuntimeException("Order cannot be cancelled at this stage");
        for (OrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();
            if (Boolean.TRUE.equals(product.getActive())) {
                product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                productRepository.save(product);
            }
            orderItem.setStatus(OrderItemStatus.CANCELLED);
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public List<com.vegetablemarket.dto.SellerOrderResponse> getSellerOrders(String email) {
        User seller = getSeller(email);
        return orderRepository.findOrdersBySellerId(seller.getId()).stream().map(order -> toSellerOrderResponse(order, seller.getId())).toList();
    }

    public com.vegetablemarket.dto.SellerOrderResponse getSellerOrder(String email, Long orderId) {
        User seller = getSeller(email);
        Order order = orderRepository.findByIdWithItems(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getItems().stream().noneMatch(item -> item.getProduct().getSellerId().equals(seller.getId()))) throw new RuntimeException("You are not authorized to access this order");
        return toSellerOrderResponse(order, seller.getId());
    }

    @Transactional
    public com.vegetablemarket.dto.SellerOrderResponse updateSellerOrderStatus(String email, Long orderId, OrderStatus newStatus) {
        User seller = getSeller(email);
        Order order = orderRepository.findByIdWithItems(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItem> sellerItems = order.getItems().stream()
                .filter(item -> item.getProduct().getSellerId().equals(seller.getId()))
                .toList();
        if (sellerItems.isEmpty()) throw new RuntimeException("You are not authorized to update this order");

        for (OrderItem item : sellerItems) {
            validateSellerStatusChange(getEffectiveItemStatus(item), newStatus);
        }
        for (OrderItem item : sellerItems) {
            item.setStatus(toItemStatus(newStatus));
        }

        order.setStatus(deriveOverallOrderStatus(order.getItems()));
        return toSellerOrderResponse(orderRepository.save(order), seller.getId());
    }

    private User getSeller(String email) {
        User seller = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if (!"SELLER".equalsIgnoreCase(seller.getRole())) throw new RuntimeException("Only sellers can access seller orders");
        return seller;
    }

    private void validateSellerStatusChange(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED) throw new RuntimeException("Cancelled orders cannot be updated");
        if (currentStatus == OrderStatus.DELIVERED) throw new RuntimeException("Delivered orders cannot be updated");
        if (newStatus == OrderStatus.PLACED) throw new RuntimeException("Seller cannot move an order back to PLACED");
        if (newStatus == OrderStatus.CANCELLED) throw new RuntimeException("Seller cannot cancel an order using this API");
        if (currentStatus == OrderStatus.PLACED && newStatus != OrderStatus.CONFIRMED) throw new RuntimeException("PLACED orders can only be moved to CONFIRMED");
        if (currentStatus == OrderStatus.CONFIRMED && newStatus != OrderStatus.SHIPPED) throw new RuntimeException("CONFIRMED orders can only be moved to SHIPPED");
        if (currentStatus == OrderStatus.SHIPPED && newStatus != OrderStatus.DELIVERED) throw new RuntimeException("SHIPPED orders can only be moved to DELIVERED");
    }

    private OrderItemStatus getEffectiveItemStatus(OrderItem item) {
        if (item.getStatus() != null) return item.getStatus();
        OrderStatus orderStatus = item.getOrder().getStatus();
        if (orderStatus == null) return OrderItemStatus.PLACED;
        return toItemStatus(orderStatus);
    }

    private OrderItemStatus toItemStatus(OrderStatus status) {
        return switch (status) {
            case PLACED -> OrderItemStatus.PLACED;
            case CONFIRMED -> OrderItemStatus.CONFIRMED;
            case SHIPPED -> OrderItemStatus.SHIPPED;
            case DELIVERED -> OrderItemStatus.DELIVERED;
            case CANCELLED -> OrderItemStatus.CANCELLED;
        };
    }

    private OrderStatus deriveOverallOrderStatus(List<OrderItem> items) {
        if (items.isEmpty()) return OrderStatus.PLACED;

        List<OrderItemStatus> statuses = items.stream().map(this::getEffectiveItemStatus).toList();
        if (statuses.stream().allMatch(status -> status == OrderItemStatus.CANCELLED)) return OrderStatus.CANCELLED;
        if (statuses.stream().allMatch(status -> status == OrderItemStatus.DELIVERED || status == OrderItemStatus.CANCELLED)) return OrderStatus.DELIVERED;
        if (statuses.stream().anyMatch(status -> status == OrderItemStatus.SHIPPED || status == OrderItemStatus.DELIVERED)) return OrderStatus.SHIPPED;
        if (statuses.stream().anyMatch(status -> status == OrderItemStatus.CONFIRMED)) return OrderStatus.CONFIRMED;
        return OrderStatus.PLACED;
    }

    private com.vegetablemarket.dto.SellerOrderResponse toSellerOrderResponse(Order order, Long sellerId) {
        com.vegetablemarket.dto.SellerOrderResponse response = new com.vegetablemarket.dto.SellerOrderResponse();
        response.setOrderId(order.getId());
        response.setCustomerId(order.getUser().getId());
        response.setCustomerName(order.getUser().getFullName());
        response.setCustomerEmail(order.getUser().getEmail());
        response.setStatus(deriveSellerOrderStatus(order, sellerId));
        response.setCreatedAt(order.getCreatedAt());
        List<com.vegetablemarket.dto.SellerOrderItemResponse> items = order.getItems().stream()
                .filter(item -> item.getProduct().getSellerId().equals(sellerId))
                .map(item -> {
                    com.vegetablemarket.dto.SellerOrderItemResponse r = new com.vegetablemarket.dto.SellerOrderItemResponse();
                    r.setOrderItemId(item.getId());
                    r.setProductId(item.getProduct().getId());
                    r.setProductName(item.getProduct().getName());
                    r.setQuantity(item.getQuantity());
                    r.setPrice(item.getPrice());
                    r.setStatus(getEffectiveItemStatus(item));
                    return r;
                }).toList();
        response.setItems(items);
        response.setSellerOrderTotal(items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum());
        return response;
    }

    private OrderStatus deriveSellerOrderStatus(Order order, Long sellerId) {
        List<OrderItem> sellerItems = order.getItems().stream()
                .filter(item -> item.getProduct().getSellerId().equals(sellerId))
                .toList();
        if (sellerItems.isEmpty()) return order.getStatus();
        return deriveOverallOrderStatus(sellerItems);
    }
}
