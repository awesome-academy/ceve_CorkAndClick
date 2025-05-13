package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderItemResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.mapper.ToDtoMappers;
import com.sun.wineshop.model.entity.*;
import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.OrderRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.service.BaseService;
import com.sun.wineshop.service.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl extends BaseService implements OrderService {

    public OrderServiceImpl(
            CartRepository cartRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository
    ) {
        super(productRepository, cartRepository, orderRepository);
    }

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
    public OrderDetailResponse getOrderDetail(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return ToDtoMappers.toOrderDetailResponse(order);
    }
}
