package com.idamart.service;

import com.idamart.dto.AdminStatsResponse;
import com.idamart.entity.OrderStatus;
import com.idamart.entity.Role;
import com.idamart.repository.OrderRepository;
import com.idamart.repository.ProductRepository;
import com.idamart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Collects the dashboard numbers with a handful of cheap COUNT / SUM queries. */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    /** "Only N left" threshold, the same one the website uses for its yellow badge. */
    public static final int LOW_STOCK_LIMIT = 5;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                orderRepository.sumTotalAmountExcluding(OrderStatus.CANCELLED),
                orderRepository.count(),
                orderRepository.countByStatusIn(List.of(OrderStatus.PLACED, OrderStatus.CONFIRMED)),
                userRepository.countByRole(Role.CUSTOMER),
                productRepository.countByActiveTrue(),
                productRepository.countByActiveTrueAndStockBetween(1, LOW_STOCK_LIMIT),
                productRepository.countByActiveTrueAndStock(0)
        );
    }
}
