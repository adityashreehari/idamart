package com.idamart.entity;

/**
 * The life of an order:
 *
 *   PLACED -> CONFIRMED -> SHIPPED -> DELIVERED
 *      \           \
 *       +-----------+--> CANCELLED
 *
 * The rules live inside the enum itself, so they can't be forgotten anywhere else.
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case PLACED -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;   // final states
        };
    }

    /** Customers may cancel only before the order has shipped. */
    public boolean isCancellableByCustomer() {
        return this == PLACED || this == CONFIRMED;
    }
}
