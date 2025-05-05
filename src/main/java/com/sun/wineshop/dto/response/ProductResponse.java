package com.sun.wineshop.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
    Long id,
    String name,
    String description,
    String imageUrl,
    Double price,
    String origin,
    Integer volume,
    Integer stockQuantity,
    Double alcoholPercentage,
    List<CategoryResponse> categories,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
