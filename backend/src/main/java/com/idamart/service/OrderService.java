package com.idamart.service;

import com.idamart.dto.CheckoutRequest;
import com.idamart.dto.OrderResponse;
import com.idamart.dto.PageResponse;
import com.idamart.entity.OrderStatus;

public interface OrderService {

    // ---------- customer ----------

    /** Turns the cart into an order: checks stock, reduces stock, empties the cart. All or nothing. */
    OrderResponse checkout(Long userId, CheckoutRequest request);

    PageResponse<OrderResponse> getMyOrders(Long userId, int page, int size);

    OrderResponse getMyOrder(Long userId, Long orderId);

    OrderResponse cancelMyOrder(Long userId, Long orderId);

    // ---------- admin ----------

    PageResponse<OrderResponse> getAllOrders(OrderStatus status, int page, int size);

    OrderResponse getOrder(Long orderId);

    OrderResponse updateStatus(Long orderId, OrderStatus newStatus);
}
