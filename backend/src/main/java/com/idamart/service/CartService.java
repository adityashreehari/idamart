package com.idamart.service;

import com.idamart.dto.CartItemRequest;
import com.idamart.dto.CartResponse;

public interface CartService {

    CartResponse getCart(Long userId);

    /** Adds the product, or increases the quantity if it's already in the cart. */
    CartResponse addItem(Long userId, CartItemRequest request);

    CartResponse updateQuantity(Long userId, Long productId, int quantity);

    CartResponse removeItem(Long userId, Long productId);

    void clearCart(Long userId);
}
