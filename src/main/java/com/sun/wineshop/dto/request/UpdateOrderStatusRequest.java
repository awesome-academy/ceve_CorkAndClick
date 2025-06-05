package com.sun.wineshop.dto.request;

import com.sun.wineshop.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateOrderStatusRequest(
        @NotNull(message = "ORDER_STATUS_REQUIRED")
        OrderStatus status,
        String reason
) {
}
