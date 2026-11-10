package com.idamart.repository;

import com.idamart.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Search + filter in one JPQL query. Any null parameter is ignored,
     * so the same method serves the plain listing and the filtered listing.
     * @EntityGraph loads each product's category in the same query (no N+1).
     */
    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND (:keyword IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.animeSeries) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:anime IS NULL OR LOWER(p.animeSeries) = LOWER(:anime))
            """)
    Page<Product> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("anime") String anime,
                         Pageable pageable);

    @Query("SELECT DISTINCT p.animeSeries FROM Product p WHERE p.active = true ORDER BY p.animeSeries")
    List<String> findDistinctAnimeSeries();

    boolean existsByCategoryId(Long categoryId);

    boolean existsByNameIgnoreCase(String name);

    /**
     * Loads products AND locks their rows until the transaction ends (SELECT ... FOR UPDATE).
     * Used at checkout so two customers can't both buy the last item at the same moment.
     * Ordered by id so transactions always lock in the same order (avoids deadlocks).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id")
    List<Product> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
