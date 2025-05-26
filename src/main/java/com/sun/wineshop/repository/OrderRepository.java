package com.sun.wineshop.repository;

import com.sun.wineshop.dto.response.MonthlyRevenueResponse;
import com.sun.wineshop.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    @Query("""
        SELECT new com.sun.wineshop.dto.response.MonthlyRevenueResponse(
            YEAR(o.createdAt),
            MONTH(o.createdAt),
            SUM(o.totalAmount)
        )
        FROM Order o
        WHERE o.status = com.sun.wineshop.model.enums.OrderStatus.DELIVERED
        GROUP BY YEAR(o.createdAt), MONTH(o.createdAt)
        ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)
    """)
    List<MonthlyRevenueResponse> getMonthlyRevenue();
}
