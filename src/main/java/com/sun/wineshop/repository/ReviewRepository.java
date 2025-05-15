package com.sun.wineshop.repository;
import com.sun.wineshop.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}
