package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderResponse;

public interface OrderService {
    OrderResponse placeOrder(PlaceOrderRequest request);
    OrderDetailResponse getOrderDetail(Long orderId, Long userId);
}
