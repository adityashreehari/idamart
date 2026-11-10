package com.idamart.controller;

import com.idamart.dto.CartItemRequest;
import com.idamart.dto.CartResponse;
import com.idamart.dto.QuantityRequest;
import com.idamart.security.AppUserPrincipal;
import com.idamart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * The logged-in user's cart. Needs a token.
 * The user id ALWAYS comes from the token (never from the URL or body),
 * so nobody can touch someone else's cart.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // GET /api/cart
    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal AppUserPrincipal me) {
        return cartService.getCart(me.getId());
    }

    // POST /api/cart/items   body: { "productId": 2, "quantity": 1 }
    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal AppUserPrincipal me,
                                @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(me.getId(), request);
    }

    // PUT /api/cart/items/2   body: { "quantity": 3 }
    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(@AuthenticationPrincipal AppUserPrincipal me,
                                       @PathVariable Long productId,
                                       @Valid @RequestBody QuantityRequest request) {
        return cartService.updateQuantity(me.getId(), productId, request.quantity());
    }

    // DELETE /api/cart/items/2
    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@AuthenticationPrincipal AppUserPrincipal me,
                                   @PathVariable Long productId) {
        return cartService.removeItem(me.getId(), productId);
    }

    // DELETE /api/cart
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal AppUserPrincipal me) {
        cartService.clearCart(me.getId());
        return ResponseEntity.noContent().build();
    }
}
