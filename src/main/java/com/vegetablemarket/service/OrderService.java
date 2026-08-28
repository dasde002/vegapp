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

}
    


