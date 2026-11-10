package com.idamart.dto;

import com.idamart.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

/** Admin changes an order's status, e.g. { "status": "SHIPPED" } */
public record OrderStatusUpdateRequest(

        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
