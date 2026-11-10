package com.idamart.service;

import com.idamart.dto.AdminStatsResponse;
import com.idamart.entity.OrderStatus;
import com.idamart.entity.Role;
import com.idamart.repository.OrderRepository;
import com.idamart.repository.ProductRepository;
import com.idamart.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @Test
    @DisplayName("Dashboard numbers: revenue skips cancelled orders, 'to process' = PLACED + CONFIRMED")
    void collectsStats() {
        when(orderRepository.sumTotalAmountExcluding(OrderStatus.CANCELLED)).thenReturn(new BigDecimal("438.00"));
        when(orderRepository.count()).thenReturn(3L);
        when(orderRepository.countByStatusIn(List.of(OrderStatus.PLACED, OrderStatus.CONFIRMED))).thenReturn(2L);
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(5L);
        when(productRepository.countByActiveTrue()).thenReturn(20L);
        when(productRepository.countByActiveTrueAndStockBetween(1, AdminStatsService.LOW_STOCK_LIMIT)).thenReturn(1L);
        when(productRepository.countByActiveTrueAndStock(0)).thenReturn(1L);

        AdminStatsResponse stats = adminStatsService.getStats();

        assertThat(stats.revenue()).isEqualByComparingTo(new BigDecimal("438.00"));
        assertThat(stats.totalOrders()).isEqualTo(3);
        assertThat(stats.ordersToProcess()).isEqualTo(2);
        assertThat(stats.customers()).isEqualTo(5);
        assertThat(stats.activeProducts()).isEqualTo(20);
        assertThat(stats.lowStockProducts()).isEqualTo(1);
        assertThat(stats.outOfStockProducts()).isEqualTo(1);
    }
}
