package com.idamart.mapper;

import com.idamart.dto.CartItemResponse;
import com.idamart.dto.CartResponse;
import com.idamart.dto.CategoryResponse;
import com.idamart.dto.OrderItemResponse;
import com.idamart.dto.OrderResponse;
import com.idamart.dto.ProductResponse;
import com.idamart.entity.CartItem;
import com.idamart.entity.Category;
import com.idamart.entity.Order;
import com.idamart.entity.OrderItem;
import com.idamart.entity.Product;

import java.math.BigDecimal;
import java.util.List;

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

    // ---------------- cart ----------------

    public static CartItemResponse toCartItemResponse(CartItem item) {
        Product p = item.getProduct();
        boolean available = Boolean.TRUE.equals(p.getActive()) && p.getStock() >= item.getQuantity();
        return new CartItemResponse(
                p.getId(),
                p.getName(),
                p.getAnimeSeries(),
                p.getImageUrl(),
                p.getPrice(),
                item.getQuantity(),
                lineTotal(p.getPrice(), item.getQuantity()),
                p.getStock(),
                available
        );
    }

    public static CartResponse toCartResponse(List<CartItem> items) {
        List<CartItemResponse> lines = items.stream()
                .map(EntityMapper::toCartItemResponse)
                .toList();
        int totalItems = lines.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal total = lines.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(lines, totalItems, total);
    }

    // ---------------- orders ----------------

    public static OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProductName(),      // snapshot from purchase time
                item.getUnitPrice(),        // snapshot from purchase time
                item.getQuantity(),
                lineTotal(item.getUnitPrice(), item.getQuantity())
        );
    }

    public static OrderResponse toOrderResponse(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getStatus().name(),
                o.getTotalAmount(),
                o.getShippingAddress(),
                o.getCreatedAt(),
                o.getUser().getFullName(),
                o.getUser().getEmail(),
                o.getItems().stream().map(EntityMapper::toOrderItemResponse).toList()
        );
    }

    public static BigDecimal lineTotal(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
