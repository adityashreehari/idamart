package com.idamart.dto;

import java.math.BigDecimal;

/**
 * One line in the cart.
 * "available" is false if the product was removed or doesn't have enough stock anymore,
 * so the website can warn the customer before checkout.
 */
public record CartItemResponse(
        Long productId,
        String productName,
        String animeSeries,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal,
        int availableStock,
        boolean available
) {
}
