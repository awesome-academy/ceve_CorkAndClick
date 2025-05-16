package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.request.UpdateOrderStatusRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderItemResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.dto.response.OrderSummaryResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.*;
import com.sun.wineshop.model.enums.OrderStatus;
import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.OrderRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (cart.getItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        Order order = Order.builder()
                .userId(userId)
                .recipientName(request.recipientName())
                .address(request.address())
                .phoneNumber(request.phoneNumber())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product == null || product.getId() == null || !productRepository.existsById(product.getId())) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getProduct().getPrice())
                    .build();
            order.getOrderItems().add(orderItem);
        }

        orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(ToDtoMappers::toOrderItemResponse).toList();

        return new OrderResponse(order.getId(), totalAmount, order.getStatus().name(), itemResponses);
    }

    @Override
    public OrderDetailResponse show(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return ToDtoMappers.toOrderDetailResponse(order);
    }

    @Override
    public Page<OrderSummaryResponse> getOrderHistory(Long userId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAllByUserId(userId, pageable);

        return orderPage.map(ToDtoMappers::toOrderSummaryResponse);
    }

    @Override
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    public Page<OrderDetailResponse> getOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orders;

        if (status != null) {
            orders = orderRepository.findOrderByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(ToDtoMappers::toOrderDetailResponse);
    }

    @Override
    public OrderDetailResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return ToDtoMappers.toOrderDetailResponse(order);
    }

    public OrderDetailResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (request.status() == OrderStatus.REJECTED && (request.reason() == null || request.reason().isBlank())) {
            throw new AppException(ErrorCode.ORDER_REJECT_REASON_REQUIRED);
        }

        order.setStatus(request.status());

        if (request.status() == OrderStatus.REJECTED) {
            order.setRejectReason(request.reason());
        } else {
            order.setRejectReason(null);
        }
        Order saved = orderRepository.save(order);

        return ToDtoMappers.toOrderDetailResponse(saved);
    }
}
