package com.idamart.dto;

import java.math.BigDecimal;

/** The numbers on the admin dashboard's stat cards. */
public record AdminStatsResponse(
        BigDecimal revenue,
        long totalOrders,
        long ordersToProcess,
        long customers,
        long activeProducts,
        long lowStockProducts,
        long outOfStockProducts
) {
}
