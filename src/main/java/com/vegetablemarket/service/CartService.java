package com.vegetablemarket.service;

import com.vegetablemarket.entity.CartItem;
import com.vegetablemarket.entity.Product;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.CartItemRepository;
import com.vegetablemarket.repository.ProductRepository;
import com.vegetablemarket.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;


    // ADD PRODUCT TO CART
    public CartItem addToCart(String email, Long productId, Integer quantity) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository
                .findByUserAndProduct(user, product)
                .orElse(null);

        int newQuantity;

        if (cartItem != null) {

            newQuantity = cartItem.getQuantity() + quantity;

        } else {

            newQuantity = quantity;
        }

        // STOCK VALIDATION
        if (newQuantity > product.getStockQuantity()) {
            throw new RuntimeException(
                    "Insufficient stock. Available stock: "
                            + product.getStockQuantity()
            );
        }

        if (cartItem != null) {

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = new CartItem();

            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        }

        return cartItemRepository.save(cartItem);
    }


    // GET CART
    public List<CartItem> getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartItemRepository.findByUser(user);
    }


    // UPDATE CART QUANTITY
    public CartItem updateQuantity(
            String email,
            Long cartItemId,
            Integer quantity) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Unauthorized cart item");
        }

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        Product product = cartItem.getProduct();

        // STOCK VALIDATION
        if (quantity > product.getStockQuantity()) {
            throw new RuntimeException(
                    "Insufficient stock. Available stock: "
                            + product.getStockQuantity()
            );
        }

        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }


    // REMOVE FROM CART
    public void removeFromCart(String email, Long cartItemId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getUser().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Unauthorized cart item");
        }

        cartItemRepository.delete(cartItem);
    }
    

    // CLEAR CART
    public void clearCart(String email) {

       User user = userRepository.findByEmail(email)
              .orElseThrow(() -> new RuntimeException("User not found"));

       List<CartItem> cartItems = cartItemRepository.findByUser(user);

       cartItemRepository.deleteAll(cartItems);
    }  

}
