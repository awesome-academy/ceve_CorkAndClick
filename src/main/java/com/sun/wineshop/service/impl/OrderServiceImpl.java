package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.OrderItemResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.*;
import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.OrderRepository;
import com.sun.wineshop.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Transactional
    @Override
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        Cart cart = cartRepository.findByUserId(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (cart.getItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        Order order = Order.builder()
                .userId(request.userId())
                .recipientName(request.recipientName())
                .address(request.address())
                .phoneNumber(request.phoneNumber())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getProduct().getPrice())
                    .build();;
            order.getOrderItems().add(orderItem);
        }

        orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(ToDtoMappers::toOrderItemResponse).toList();

        return new OrderResponse(order.getId(), totalAmount, order.getStatus().name(), itemResponses);
    }
}
