package com.idamart.repository;

import com.idamart.entity.Order;
import com.idamart.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** A customer's own orders, newest first. */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** Admin: all orders (with the customer loaded in the same query). */
    @Override
    @EntityGraph(attributePaths = "user")
    Page<Order> findAll(Pageable pageable);

    /** Admin: orders filtered by status. */
    @EntityGraph(attributePaths = "user")
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // ---- admin dashboard numbers ----

    long countByStatusIn(Collection<OrderStatus> statuses);

    /** Money from every order that wasn't cancelled. COALESCE turns "no orders" (NULL) into 0. */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status <> :excluded")
    BigDecimal sumTotalAmountExcluding(@Param("excluded") OrderStatus excluded);

    /** One order with its items and customer, in a single query. */
    @Query("SELECT o FROM Order o JOIN FETCH o.user LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
