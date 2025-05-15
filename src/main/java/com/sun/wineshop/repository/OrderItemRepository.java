package com.sun.wineshop.repository;

import com.sun.wineshop.model.entity.OrderItem;
import com.sun.wineshop.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProductId(Long productId);

    @Query("""
    SELECT CASE WHEN COUNT(item) > 0 THEN true ELSE false END
    FROM OrderItem item
    WHERE item.product.id = :productId
      AND item.order.userId = :userId
      AND item.order.status = :status
    """)
    boolean hasUserOrderedProduct(Long productId, Long userId, OrderStatus status);
}
