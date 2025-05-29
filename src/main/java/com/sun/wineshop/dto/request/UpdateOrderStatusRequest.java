package com.sun.wineshop.dto.request;

import com.sun.wineshop.model.enums.OrderStatus;

public record UpdateOrderStatusRequest(
        OrderStatus status
) {}
