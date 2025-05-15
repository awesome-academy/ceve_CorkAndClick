package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.ReviewRequest;

public interface ReviewService {
    void addReview(Long userId, Long productId, ReviewRequest request);
}
