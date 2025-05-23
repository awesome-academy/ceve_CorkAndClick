package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.ReviewRequest;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.model.entity.Review;
import com.sun.wineshop.model.enums.OrderStatus;
import com.sun.wineshop.repository.OrderItemRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.repository.ReviewRepository;
import com.sun.wineshop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public void addReview(Long userId, ReviewRequest request) {
        Long productId = request.productId();

        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        boolean hasBoughtAndDelivered = orderItemRepository
                .hasUserOrderedProduct(productId, userId, OrderStatus.DELIVERED);

        if (!hasBoughtAndDelivered) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Review review = Review.builder()
                .userId(userId)
                .product(product)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        reviewRepository.save(review);
    }
}
