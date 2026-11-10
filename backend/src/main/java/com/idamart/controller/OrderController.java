package com.idamart.controller;

import com.idamart.dto.CheckoutRequest;
import com.idamart.dto.OrderResponse;
import com.idamart.dto.PageResponse;
import com.idamart.security.AppUserPrincipal;
import com.idamart.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** The logged-in customer's own orders. Needs a token. */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders   body: { "shippingAddress": "..." }   -> checkout the whole cart
    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal AppUserPrincipal me,
                                                  @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.checkout(me.getId(), request));
    }

    // GET /api/orders?page=0&size=10
    @GetMapping
    public PageResponse<OrderResponse> myOrders(@AuthenticationPrincipal AppUserPrincipal me,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return orderService.getMyOrders(me.getId(), page, size);
    }

    // GET /api/orders/7
    @GetMapping("/{orderId}")
    public OrderResponse myOrder(@AuthenticationPrincipal AppUserPrincipal me,
                                 @PathVariable Long orderId) {
        return orderService.getMyOrder(me.getId(), orderId);
    }

    // PATCH /api/orders/7/cancel
    @PatchMapping("/{orderId}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AppUserPrincipal me,
                                @PathVariable Long orderId) {
        return orderService.cancelMyOrder(me.getId(), orderId);
    }
}
