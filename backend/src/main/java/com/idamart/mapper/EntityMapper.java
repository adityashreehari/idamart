package com.idamart.mapper;

import com.idamart.dto.CategoryResponse;
import com.idamart.dto.ProductResponse;
import com.idamart.entity.Category;
import com.idamart.entity.Product;

/**
 * Converts entities (database objects) into DTOs (API objects).
 * Kept in one place so controllers and services stay clean.
 * Private constructor: this is a utility class, nobody should create an instance.
 */
public final class EntityMapper {

    private EntityMapper() {
    }

    public static CategoryResponse toCategoryResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }

    public static ProductResponse toProductResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getAnimeSeries(),
                p.getPrice(),
                p.getStock(),
                p.getStock() != null && p.getStock() > 0,
                p.getImageUrl(),
                p.getCategory().getId(),
                p.getCategory().getName()
        );
    }
}
