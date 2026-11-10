package com.idamart.service.impl;

import com.idamart.TestData;
import com.idamart.dto.CartItemRequest;
import com.idamart.dto.CartResponse;
import com.idamart.entity.CartItem;
import com.idamart.entity.Product;
import com.idamart.entity.Role;
import com.idamart.entity.User;
import com.idamart.exception.BadRequestException;
import com.idamart.exception.ResourceNotFoundException;
import com.idamart.repository.CartItemRepository;
import com.idamart.repository.ProductRepository;
import com.idamart.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final long USER_ID = 7L;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private final User customer = TestData.user(USER_ID, Role.CUSTOMER);

    @Test
    @DisplayName("Adding a new product creates a cart line")
    void addItem_new() {
        Product figure = TestData.product(1, "Goku Figure", "199.00", 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(figure));
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, 1L)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(customer);

        cartService.addItem(USER_ID, new CartItemRequest(1L, 2));

        ArgumentCaptor<CartItem> saved = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(saved.capture());
        assertThat(saved.getValue().getQuantity()).isEqualTo(2);
        assertThat(saved.getValue().getProduct()).isSameAs(figure);
    }

    @Test
    @DisplayName("Adding the same product again increases the quantity instead of adding a second line")
    void addItem_existing() {
        Product figure = TestData.product(1, "Goku Figure", "199.00", 10);
        CartItem existing = TestData.cartItem(customer, figure, 3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(figure));
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, 1L)).thenReturn(Optional.of(existing));

        cartService.addItem(USER_ID, new CartItemRequest(1L, 2));

        assertThat(existing.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(existing);
    }

    @Test
    @DisplayName("You can't put more in the cart than is on the shelf")
    void addItem_moreThanStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(TestData.product(1, "Goku Figure", "199.00", 1)));
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(USER_ID, new CartItemRequest(1L, 2)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only 1 left");
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Max 10 of one item, even if adding in small steps")
    void addItem_overTen() {
        Product figure = TestData.product(1, "Goku Figure", "199.00", 50);
        when(productRepository.findById(1L)).thenReturn(Optional.of(figure));
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, 1L))
                .thenReturn(Optional.of(TestData.cartItem(customer, figure, 9)));

        assertThatThrownBy(() -> cartService.addItem(USER_ID, new CartItemRequest(1L, 2)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at most 10");
    }

    @Test
    @DisplayName("Hidden (soft-deleted) products can't be added")
    void addItem_inactive() {
        Product hidden = TestData.product(1, "Old Figure", "10.00", 5);
        hidden.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(hidden));

        assertThatThrownBy(() -> cartService.addItem(USER_ID, new CartItemRequest(1L, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("The cart total is the sum of price x quantity for every line")
    void getCart_totals() {
        CartItem a = TestData.cartItem(customer, TestData.product(1, "A", "10.50", 10), 2);   // 21.00
        CartItem b = TestData.cartItem(customer, TestData.product(2, "B", "5.00", 10), 1);    //  5.00
        when(cartItemRepository.findByUserIdWithProduct(USER_ID)).thenReturn(List.of(a, b));

        CartResponse cart = cartService.getCart(USER_ID);

        assertThat(cart.totalItems()).isEqualTo(3);
        assertThat(cart.totalAmount()).isEqualByComparingTo(new BigDecimal("26.00"));
        assertThat(cart.items()).hasSize(2);
    }
}
