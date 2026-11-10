package com.idamart.service.impl;

import com.idamart.TestData;
import com.idamart.dto.CheckoutRequest;
import com.idamart.dto.OrderResponse;
import com.idamart.entity.Order;
import com.idamart.entity.OrderItem;
import com.idamart.entity.OrderStatus;
import com.idamart.entity.Product;
import com.idamart.entity.Role;
import com.idamart.entity.User;
import com.idamart.exception.BadRequestException;
import com.idamart.exception.ResourceNotFoundException;
import com.idamart.repository.CartItemRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final long USER_ID = 7L;
    private static final CheckoutRequest ADDRESS = new CheckoutRequest("Flat 12, Marina Tower, Dubai");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private final User customer = TestData.user(USER_ID, Role.CUSTOMER);

    // ------------------------------------------------------------------ checkout

    @Test
    @DisplayName("Checkout with an empty cart is rejected")
    void checkout_emptyCart() {
        when(cartItemRepository.findProductIdsByUserId(USER_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.checkout(USER_ID, ADDRESS))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty");
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Checkout: creates the order, copies prices, reduces stock, empties the cart")
    void checkout_success() {
        Product figure = TestData.product(1, "Goku Figure", "49.99", 10);
        when(cartItemRepository.findProductIdsByUserId(USER_ID)).thenReturn(List.of(1L));
        when(productRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(figure));
        when(cartItemRepository.findByUserIdWithProduct(USER_ID))
                .thenReturn(List.of(TestData.cartItem(customer, figure, 2)));
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderResponse order = orderService.checkout(USER_ID, ADDRESS);

        assertThat(order.id()).isEqualTo(100L);
        assertThat(order.status()).isEqualTo("PLACED");
        assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("99.98"));
        assertThat(order.items()).hasSize(1);
        assertThat(order.items().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("49.99"));
        assertThat(figure.getStock()).isEqualTo(8);                    // 10 - 2
        verify(cartItemRepository).deleteAllByUserId(USER_ID);       // cart emptied
    }

    @Test
    @DisplayName("Checkout with too little stock fails and changes NOTHING (all or nothing)")
    void checkout_notEnoughStock() {
        Product figure = TestData.product(1, "Goku Figure", "49.99", 1);
        when(cartItemRepository.findProductIdsByUserId(USER_ID)).thenReturn(List.of(1L));
        when(productRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(figure));
        when(cartItemRepository.findByUserIdWithProduct(USER_ID))
                .thenReturn(List.of(TestData.cartItem(customer, figure, 2)));

        assertThatThrownBy(() -> orderService.checkout(USER_ID, ADDRESS))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only 1 left");
        assertThat(figure.getStock()).isEqualTo(1);                  // untouched
        verify(orderRepository, never()).save(any());
        verify(cartItemRepository, never()).deleteAllByUserId(anyLong());
    }

    // ------------------------------------------------------------------ cancelling

    @Test
    @DisplayName("Cancelling a PLACED order puts the stock back")
    void cancel_restoresStock() {
        Product figure = TestData.product(1, "Goku Figure", "49.99", 8);
        Order order = orderOf(customer, OrderStatus.PLACED, figure, 2);
        when(orderRepository.findByIdWithItems(5L)).thenReturn(Optional.of(order));
        when(productRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(figure));

        OrderResponse response = orderService.cancelMyOrder(USER_ID, 5L);

        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(figure.getStock()).isEqualTo(10);                 // 8 + 2
    }

    @Test
    @DisplayName("A SHIPPED order can't be cancelled by the customer")
    void cancel_shipped() {
        Order order = orderOf(customer, OrderStatus.SHIPPED, TestData.product(1, "X", "1.00", 1), 1);
        when(orderRepository.findByIdWithItems(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelMyOrder(USER_ID, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("can no longer be cancelled");
    }

    @Test
    @DisplayName("Someone else's order looks 'not found' (404), so its existence isn't leaked")
    void getMyOrder_otherCustomer() {
        User stranger = TestData.user(99, Role.CUSTOMER);
        Order order = orderOf(stranger, OrderStatus.PLACED, TestData.product(1, "X", "1.00", 1), 1);
        when(orderRepository.findByIdWithItems(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getMyOrder(USER_ID, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ------------------------------------------------------------------ admin status

    @Test
    @DisplayName("Admin moves PLACED -> CONFIRMED")
    void updateStatus_valid() {
        Order order = orderOf(customer, OrderStatus.PLACED, TestData.product(1, "X", "1.00", 1), 1);
        when(orderRepository.findByIdWithItems(5L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateStatus(5L, OrderStatus.CONFIRMED);

        assertThat(response.status()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("Admin can't skip from PLACED straight to DELIVERED")
    void updateStatus_skip() {
        Order order = orderOf(customer, OrderStatus.PLACED, TestData.product(1, "X", "1.00", 1), 1);
        when(orderRepository.findByIdWithItems(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(5L, OrderStatus.DELIVERED))
                .isInstanceOf(BadRequestException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    // ------------------------------------------------------------------ helper

    private Order orderOf(User owner, OrderStatus status, Product product, int quantity) {
        Order order = Order.builder()
                .user(owner)
                .status(status)
                .shippingAddress("Somewhere 12345")
                .totalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build();
        order.setId(5L);
        order.addItem(OrderItem.builder()
                .product(product)
                .productName(product.getName())
                .unitPrice(product.getPrice())
                .quantity(quantity)
                .build());
        return order;
    }
}
