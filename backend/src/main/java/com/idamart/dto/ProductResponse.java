package com.idamart.dto;

import java.math.BigDecimal;

/**
 * What the API RETURNS for a product.
 * We never return the JPA entity directly: that would leak internal fields
 * and can trigger lazy-loading errors when converted to JSON.
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        String animeSeries,
        BigDecimal price,
        Integer stock,
        boolean inStock,
        String imageUrl,
        Long categoryId,
        String categoryName
) {
}
