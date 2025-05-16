package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.request.UpdateOrderStatusRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.dto.response.OrderSummaryResponse;
import com.sun.wineshop.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse placeOrder(Long userId, PlaceOrderRequest request);
    OrderDetailResponse show(Long orderId, Long userId);
    Page<OrderSummaryResponse> getOrderHistory(Long userId, int pageNumber, int pageSize);
    void cancelOrder(Long orderId, Long userId);
    Page<OrderDetailResponse> getOrders(OrderStatus status, Pageable pageable);
    OrderDetailResponse getOrderById(Long orderId);
    OrderDetailResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}
