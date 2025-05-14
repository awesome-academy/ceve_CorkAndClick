package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;

public interface OrderService {
    OrderResponse placeOrder(PlaceOrderRequest request);
    OrderDetailResponse show(Long orderId, Long userId);
    Page<OrderSummaryResponse> getOrderHistory(Long userId, int pageNumber, int pageSize);
    void cancelOrder(Long orderId, Long userId);
}
