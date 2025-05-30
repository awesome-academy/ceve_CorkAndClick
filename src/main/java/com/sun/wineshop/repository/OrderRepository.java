package com.sun.wineshop.repository;

import com.sun.wineshop.dto.response.MonthlyOrderStats;
import com.sun.wineshop.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    @Query("""
        SELECT new com.sun.wineshop.dto.response.MonthlyOrderStats(
            YEAR(o.createdAt),
            MONTH(o.createdAt),
            COUNT(o),
            SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.status = 'REJECTED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN o.status = 'DELIVERED' THEN o.totalAmount ELSE 0 END)
        )
        FROM Order o
        GROUP BY YEAR(o.createdAt), MONTH(o.createdAt)
        ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)
    """)
    List<MonthlyOrderStats> getMonthlyOrderStatistics();
}
