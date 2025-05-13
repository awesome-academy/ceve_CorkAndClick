package com.sun.wineshop.dto.request;

import java.util.List;

public record ProductRequest(
    String name,
    String description,
    String imageUrl,
    Double price,
    String origin,
    Integer volume,
    Integer stockQuantity,
    Double alcoholPercentage,
    List<Long> categoryIds
) {}
