package com.vegetablemarket.service;

import com.vegetablemarket.entity.CartItem;
import com.vegetablemarket.entity.Order;
import com.vegetablemarket.entity.OrderItem;
import com.vegetablemarket.entity.OrderStatus;
import com.vegetablemarket.entity.Product;
import com.vegetablemarket.entity.User;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Transactional
    public Order checkout(String email) {

        // 1. Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        // 2. Get user's cart
        List<CartItem> cartItems =
                cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }


        // 3. Check stock
        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() >
                    product.getStockQuantity()) {

                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }
        }


        // 4. Create order
        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        order.setTotalAmount(0.0);

        order = orderRepository.save(order);


        // 5. Create order items
        List<OrderItem> orderItems = new ArrayList<>();

        double totalAmount = 0.0;


        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItems.add(orderItem);


            // Calculate total
            totalAmount +=
                    product.getPrice()
                            * cartItem.getQuantity();


            // 6. Reduce stock
            product.setStockQuantity(
                    product.getStockQuantity()
                            - cartItem.getQuantity()
            );

            productRepository.save(product);
        }


        // 7. Set order items
        order.setItems(orderItems);

        // 8. Set total
        order.setTotalAmount(totalAmount);

        order = orderRepository.save(order);


        // 9. Clear cart
        cartItemRepository.deleteAll(cartItems);


        // 10. Return order
        return order;
    }


    // GET MY ORDERS
    public List<Order> getMyOrders(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return orderRepository.findByUser(user);
    }


    // GET SINGLE ORDER
    public Order getOrder(String email, Long orderId) {

       User user = userRepository.findByEmail(email)
               .orElseThrow(() ->
                      new RuntimeException("User not found"));

       Order order = orderRepository.findById(orderId)
               .orElseThrow(() ->
                      new RuntimeException("Order not found"));

    // Make sure the order belongs to the logged-in user
    if (!order.getUser().getId().equals(user.getId())) {
        throw new RuntimeException("Unauthorized access to order");
    }

    return order;
   }

   
   // CANCEL ORDER
@Transactional
public Order cancelOrder(String email, Long orderId) {

    // 1. Find user
    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    // 2. Find order
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                    new RuntimeException("Order not found"));

    // 3. Make sure order belongs to logged-in user
    if (!order.getUser().getId().equals(user.getId())) {
        throw new RuntimeException(
                "Unauthorized access to order");
    }

    // 4. Check order status
    if (order.getStatus() == OrderStatus.CANCELLED) {
        throw new RuntimeException(
                "Order is already cancelled");
    }

    if (order.getStatus() == OrderStatus.SHIPPED ||
            order.getStatus() == OrderStatus.DELIVERED) {

        throw new RuntimeException(
                "Order cannot be cancelled at this stage");
    }

    // 5. Restore product stock
    for (OrderItem orderItem : order.getItems()) {

        Product product = orderItem.getProduct();

        product.setStockQuantity(
                product.getStockQuantity()
                        + orderItem.getQuantity()
        );

        productRepository.save(product);
    }

    // 6. Change status
    order.setStatus(OrderStatus.CANCELLED);

    // 7. Save order
    return orderRepository.save(order);
    }  


// ============================================================
// SELLER ORDER MANAGEMENT
// ============================================================

public List<com.vegetablemarket.dto.SellerOrderResponse> getSellerOrders(
        String email) {

    User seller = getSeller(email);

    List<Order> orders =
            orderRepository.findOrdersBySellerId(seller.getId());

    return orders.stream()
            .map(order -> toSellerOrderResponse(order, seller.getId()))
            .toList();
}


public com.vegetablemarket.dto.SellerOrderResponse getSellerOrder(
        String email,
        Long orderId) {

    User seller = getSeller(email);

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                    new RuntimeException("Order not found"));

    // Make sure this seller has at least one product in the order
    boolean sellerOwnsItem = order.getItems()
            .stream()
            .anyMatch(item ->
                    item.getProduct()
                            .getSellerId()
                            .equals(seller.getId()));

    if (!sellerOwnsItem) {
        throw new RuntimeException(
                "You are not authorized to access this order");
    }

    return toSellerOrderResponse(order, seller.getId());
}


@Transactional
public com.vegetablemarket.dto.SellerOrderResponse updateSellerOrderStatus(
        String email,
        Long orderId,
        OrderStatus newStatus) {

    User seller = getSeller(email);

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                    new RuntimeException("Order not found"));

    // Check whether seller has products in this order
    boolean sellerOwnsItem = order.getItems()
            .stream()
            .anyMatch(item ->
                    item.getProduct()
                            .getSellerId()
                            .equals(seller.getId()));

    if (!sellerOwnsItem) {
        throw new RuntimeException(
                "You are not authorized to update this order");
    }

    // Because Order currently has ONE status for the entire order,
    // don't allow one seller to change another seller's fulfillment.
    boolean allItemsBelongToSeller = order.getItems()
            .stream()
            .allMatch(item ->
                    item.getProduct()
                            .getSellerId()
                            .equals(seller.getId()));

    if (!allItemsBelongToSeller) {
        throw new RuntimeException(
                "This order contains products from multiple sellers. "
                + "Seller-specific status management is not available "
                + "for this order yet.");
    }

    validateSellerStatusChange(
            order.getStatus(),
            newStatus);

    order.setStatus(newStatus);

    Order savedOrder = orderRepository.save(order);

    return toSellerOrderResponse(
            savedOrder,
            seller.getId());
}


private User getSeller(String email) {

    User seller = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    if (!"SELLER".equalsIgnoreCase(seller.getRole())) {
        throw new RuntimeException(
                "Only sellers can access seller orders");
    }

    return seller;
}


private void validateSellerStatusChange(
        OrderStatus currentStatus,
        OrderStatus newStatus) {

    if (currentStatus == OrderStatus.CANCELLED) {
        throw new RuntimeException(
                "Cancelled orders cannot be updated");
    }

    if (currentStatus == OrderStatus.DELIVERED) {
        throw new RuntimeException(
                "Delivered orders cannot be updated");
    }

    if (newStatus == OrderStatus.PLACED) {
        throw new RuntimeException(
                "Seller cannot move an order back to PLACED");
    }

    if (newStatus == OrderStatus.CANCELLED) {
        throw new RuntimeException(
                "Seller cannot cancel an order using this API");
    }

    if (currentStatus == OrderStatus.PLACED
            && newStatus != OrderStatus.CONFIRMED) {

        throw new RuntimeException(
                "PLACED orders can only be moved to CONFIRMED");
    }

    if (currentStatus == OrderStatus.CONFIRMED
            && newStatus != OrderStatus.SHIPPED) {

        throw new RuntimeException(
                "CONFIRMED orders can only be moved to SHIPPED");
    }

    if (currentStatus == OrderStatus.SHIPPED
            && newStatus != OrderStatus.DELIVERED) {

        throw new RuntimeException(
                "SHIPPED orders can only be moved to DELIVERED");
    }
}


private com.vegetablemarket.dto.SellerOrderResponse
toSellerOrderResponse(
        Order order,
        Long sellerId) {

    com.vegetablemarket.dto.SellerOrderResponse response =
            new com.vegetablemarket.dto.SellerOrderResponse();

    response.setOrderId(order.getId());
    response.setCustomerId(order.getUser().getId());
    response.setCustomerName(order.getUser().getFullName());
    response.setCustomerEmail(order.getUser().getEmail());
    response.setStatus(order.getStatus());
    response.setCreatedAt(order.getCreatedAt());

    List<com.vegetablemarket.dto.SellerOrderItemResponse> items =
            order.getItems()
                    .stream()
                    .filter(item ->
                            item.getProduct()
                                    .getSellerId()
                                    .equals(sellerId))
                    .map(item -> {

                        com.vegetablemarket.dto.SellerOrderItemResponse itemResponse =
                                new com.vegetablemarket.dto.SellerOrderItemResponse();

                        itemResponse.setOrderItemId(item.getId());
                        itemResponse.setProductId(
                                item.getProduct().getId());
                        itemResponse.setProductName(
                                item.getProduct().getName());
                        itemResponse.setQuantity(
                                item.getQuantity());
                        itemResponse.setPrice(
                                item.getPrice());

                        return itemResponse;
                    })
                    .toList();

    response.setItems(items);

    double sellerTotal = items.stream()
            .mapToDouble(item ->
                    item.getPrice() * item.getQuantity())
            .sum();

    response.setSellerOrderTotal(sellerTotal);

    return response;
}
}
    


