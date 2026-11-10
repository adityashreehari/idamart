package com.idamart.controller;

import com.idamart.dto.OrderResponse;
import com.idamart.dto.OrderStatusUpdateRequest;
import com.idamart.dto.PageResponse;
import com.idamart.entity.OrderStatus;
import com.idamart.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Everything under /api/admin/** is ADMIN only (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // GET /api/admin/orders?status=PLACED&page=0&size=20
    @GetMapping
    public PageResponse<OrderResponse> allOrders(@RequestParam(required = false) OrderStatus status,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return orderService.getAllOrders(status, page, size);
    }

    // GET /api/admin/orders/7
    @GetMapping("/{orderId}")
    public OrderResponse order(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    // PATCH /api/admin/orders/7/status   body: { "status": "SHIPPED" }
    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable Long orderId,
                                      @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(orderId, request.status());
    }
}
