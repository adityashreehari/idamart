package com.idamart.service.impl;

import com.idamart.dto.CheckoutRequest;
import com.idamart.dto.OrderResponse;
import com.idamart.dto.PageResponse;
import com.idamart.entity.CartItem;
import com.idamart.entity.Order;
import com.idamart.entity.OrderItem;
import com.idamart.entity.OrderStatus;
import com.idamart.entity.Product;
import com.idamart.exception.BadRequestException;
import com.idamart.exception.ResourceNotFoundException;
import com.idamart.mapper.EntityMapper;
import com.idamart.repository.CartItemRepository;
import com.idamart.repository.OrderRepository;
import com.idamart.repository.ProductRepository;
import com.idamart.repository.UserRepository;
import com.idamart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final int MAX_PAGE_SIZE = 50;

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // =====================================================================
    //  CHECKOUT
    // =====================================================================

    /**
     * @Transactional = all or nothing. If ANY item fails the stock check,
     * an exception is thrown and every change (stock, order, cart) is rolled back.
     */
    @Override
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {

        // 1. Lock the products in the cart FIRST, so their stock values are fresh
        //    and no other checkout can change them until we finish.
        List<Long> productIds = cartItemRepository.findProductIdsByUserId(userId);
        if (productIds.isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }
        Map<Long, Product> lockedProducts = productRepository.findAllByIdForUpdate(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 2. Load the cart lines (their products are the locked ones above)
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);

        // 3. Check every line before changing anything, and collect all problems at once
        List<String> problems = new ArrayList<>();
        for (CartItem line : cartItems) {
            Product product = lockedProducts.get(line.getProduct().getId());
            if (!Boolean.TRUE.equals(product.getActive())) {
                problems.add("'" + product.getName() + "' is no longer available");
            } else if (product.getStock() < line.getQuantity()) {
                problems.add("Only " + product.getStock() + " left of '" + product.getName() + "'");
            }
        }
        if (!problems.isEmpty()) {
            throw new BadRequestException(String.join("; ", problems));
        }

        // 4. Build the order, copy prices, reduce stock
        Order order = Order.builder()
                .user(userRepository.getReferenceById(userId))
                .status(OrderStatus.PLACED)
                .shippingAddress(request.shippingAddress().trim())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem line : cartItems) {
            Product product = lockedProducts.get(line.getProduct().getId());

            order.addItem(OrderItem.builder()
                    .product(product)
                    .productName(product.getName())   // snapshot
                    .unitPrice(product.getPrice())    // snapshot
                    .quantity(line.getQuantity())
                    .build());

            product.setStock(product.getStock() - line.getQuantity());
            total = total.add(EntityMapper.lineTotal(product.getPrice(), line.getQuantity()));
        }
        order.setTotalAmount(total);

        // 5. Save the order (items are saved too, thanks to cascade) and empty the cart
        Order saved = orderRepository.save(order);
        cartItemRepository.deleteAllByUserId(userId);

        return EntityMapper.toOrderResponse(saved);
    }

    // =====================================================================
    //  CUSTOMER
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(Long userId, int page, int size) {
        Page<OrderResponse> result = orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable(page, size, Sort.unsorted()))
                .map(EntityMapper::toOrderResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long userId, Long orderId) {
        return EntityMapper.toOrderResponse(findOwnOrderOrThrow(userId, orderId));
    }

    @Override
    @Transactional
    public OrderResponse cancelMyOrder(Long userId, Long orderId) {
        Order order = findOwnOrderOrThrow(userId, orderId);
        if (!order.getStatus().isCancellableByCustomer()) {
            throw new BadRequestException("Order " + orderId + " is " + order.getStatus()
                    + " and can no longer be cancelled");
        }
        cancelAndRestoreStock(order);
        return EntityMapper.toOrderResponse(order);
    }

    // =====================================================================
    //  ADMIN
    // =====================================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, int page, int size) {
        Pageable pageable = pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PageResponse.from(orders.map(EntityMapper::toOrderResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return EntityMapper.toOrderResponse(findOrderOrThrow(orderId));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = findOrderOrThrow(orderId);
        OrderStatus current = order.getStatus();

        if (!current.canMoveTo(newStatus)) {
            throw new BadRequestException("Cannot change order from " + current + " to " + newStatus);
        }
        if (newStatus == OrderStatus.CANCELLED) {
            cancelAndRestoreStock(order);
        } else {
            order.setStatus(newStatus);
        }
        return EntityMapper.toOrderResponse(order);
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    /** Cancelling puts the items back on the shelf. */
    private void cancelAndRestoreStock(Order order) {
        List<Long> productIds = order.getItems().stream()
                .map(item -> item.getProduct().getId())
                .toList();
        Map<Long, Product> locked = productRepository.findAllByIdForUpdate(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (OrderItem item : order.getItems()) {
            Product product = locked.get(item.getProduct().getId());
            product.setStock(product.getStock() + item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    /**
     * A customer asking for someone else's order gets 404, not 403,
     * so they can't even learn that the order exists.
     */
    private Order findOwnOrderOrThrow(Long userId, Long orderId) {
        Order order = findOrderOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return order;
    }

    private Pageable pageable(int page, int size, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
