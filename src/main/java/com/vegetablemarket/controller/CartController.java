package com.vegetablemarket.controller;

import com.vegetablemarket.entity.CartItem;
import com.vegetablemarket.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;


    // ADD PRODUCT TO CART
    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        String email = authentication.getName();

        CartItem cartItem =
                cartService.addToCart(
                        email,
                        productId,
                        quantity
                );

        return ResponseEntity.ok(cartItem);
    }


    // GET CART
    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.getCart(email)
        );
    }


    // UPDATE QUANTITY
    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItem> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                cartService.updateQuantity(
                        email,
                        cartItemId,
                        quantity
                )
        );
    }


    // REMOVE ITEM
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<String> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {

        String email = authentication.getName();

        cartService.removeFromCart(
                email,
                cartItemId
        );

        return ResponseEntity.ok(
                "Product removed from cart"
        );
    }

    // CLEAR CART
   @DeleteMapping("/clear")
   public ResponseEntity<String> clearCart(
           Authentication authentication) {

      String email = authentication.getName();

      cartService.clearCart(email);

      return ResponseEntity.ok(
              "Cart cleared successfully"
      );
    } 

}   
