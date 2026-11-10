package com.idamart.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain JUnit test: no Spring, no mocks. Checks the order "one-way road" rules. */
class OrderStatusTest {

    @Test
    @DisplayName("Orders move forward one step at a time")
    void allowedTransitions() {
        assertThat(OrderStatus.PLACED.canMoveTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canMoveTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canMoveTo(OrderStatus.DELIVERED)).isTrue();
    }

    @Test
    @DisplayName("Orders can't skip steps or go backwards")
    void blockedTransitions() {
        assertThat(OrderStatus.PLACED.canMoveTo(OrderStatus.SHIPPED)).isFalse();
        assertThat(OrderStatus.CONFIRMED.canMoveTo(OrderStatus.DELIVERED)).isFalse();
        assertThat(OrderStatus.SHIPPED.canMoveTo(OrderStatus.CONFIRMED)).isFalse();
        assertThat(OrderStatus.SHIPPED.canMoveTo(OrderStatus.CANCELLED)).isFalse();
    }

    @ParameterizedTest(name = "DELIVERED cannot move to {0}")
    @EnumSource(OrderStatus.class)
    void deliveredIsFinal(OrderStatus next) {
        assertThat(OrderStatus.DELIVERED.canMoveTo(next)).isFalse();
    }

    @ParameterizedTest(name = "CANCELLED cannot move to {0}")
    @EnumSource(OrderStatus.class)
    void cancelledIsFinal(OrderStatus next) {
        assertThat(OrderStatus.CANCELLED.canMoveTo(next)).isFalse();
    }

    @Test
    @DisplayName("Customers can cancel only before shipping")
    void customerCancellation() {
        assertThat(OrderStatus.PLACED.isCancellableByCustomer()).isTrue();
        assertThat(OrderStatus.CONFIRMED.isCancellableByCustomer()).isTrue();
        assertThat(OrderStatus.SHIPPED.isCancellableByCustomer()).isFalse();
        assertThat(OrderStatus.DELIVERED.isCancellableByCustomer()).isFalse();
    }
}
