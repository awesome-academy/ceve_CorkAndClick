package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderItemResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.dto.response.OrderSummaryResponse;
import com.sun.wineshop.dto.websocket.OrderNotification;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.*;
import com.sun.wineshop.model.enums.OrderStatus;
import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.OrderRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.service.OrderService;
import com.sun.wineshop.utils.AppConstants;
import com.sun.wineshop.utils.MessageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final MessageUtil messageUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));


        order.setStatus(newStatus);
        orderRepository.save(order);

        if (newStatus == OrderStatus.DELIVERING) {
            OrderNotification notification = new OrderNotification(
                    order.getId(),
                    order.getStatus().name(),
                    order.getTotalAmount(),
                    messageUtil.getMessage("order.status.delivering")
            );

            messagingTemplate.convertAndSend(AppConstants.WS_ORDER_TOPIC + order.getUserId(), notification);
        }
    }
}
