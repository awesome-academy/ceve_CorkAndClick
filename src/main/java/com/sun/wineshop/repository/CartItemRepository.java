package com.sun.wineshop.repository;

import com.sun.wineshop.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByProductIdIn(List<Long> productIds);
}
