package com.sun.wineshop.dto.request;

import lombok.Builder;

@Builder
public record RemoveCartItemRequest(
    Long productId
) {}
