package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.request.UpdateOrderStatusRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Cart;
import com.sun.wineshop.model.entity.CartItem;
import com.sun.wineshop.model.entity.Order;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.model.enums.OrderStatus;
import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.OrderRepository;
import com.sun.wineshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Cart cart;
    private PlaceOrderRequest request;
    private Order order;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .id(1L)
                .price(100.0)
                .build();

        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .userId(1L)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();

        request = new PlaceOrderRequest("Rio Phan", "123 Street", "0123456789");

        order = Order.builder()
                .id(1L)
                .userId(1L)
                .recipientName(request.recipientName())
                .address(request.address())
                .phoneNumber(request.phoneNumber())
                .totalAmount(200.0)
                .status(OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();
    }

    @Test
    void placeOrder_validRequest_shouldReturnOrderResponse() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        OrderResponse response = orderService.placeOrder(1L, request);

        assertNotNull(response);
        assertEquals(200.0, response.totalAmount());
        assertEquals(OrderStatus.PENDING.name(), response.status());
        assertEquals(1, response.items().size());

        verify(cartRepository).save(cart);
    }

    @Test
    void placeOrder_cartNotFound_shouldThrowAppException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> orderService.placeOrder(1L, request));

        assertEquals(ErrorCode.CART_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void placeOrder_cartEmpty_shouldThrowAppException() {
        cart.setItems(new ArrayList<>());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        AppException ex = assertThrows(AppException.class,
                () -> orderService.placeOrder(1L, request));

        assertEquals(ErrorCode.CART_EMPTY, ex.getErrorCode());
    }

    @Test
    void placeOrder_productNotExists_shouldThrowAppException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.existsById(1L)).thenReturn(false);

        AppException ex = assertThrows(AppException.class,
                () -> orderService.placeOrder(1L, request));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void show_orderOfOtherUser_shouldThrowUnauthorized() {
        Order order = Order.builder().id(1L).userId(99L).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(AppException.class, () -> orderService.show(1L, 1L));
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void cancelOrder_notPending_shouldThrow() {
        Order order = Order.builder().id(1L).userId(1L).status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        AppException ex = assertThrows(AppException.class, () -> orderService.cancelOrder(1L, 1L));
        assertEquals(ErrorCode.ORDER_CANNOT_BE_CANCELLED, ex.getErrorCode());
    }

    @Test
    void updateOrderStatus_rejectedWithoutReason_shouldThrow() {
        Order order = Order.builder().id(1L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.REJECTED, "");

        AppException ex = assertThrows(AppException.class, () -> orderService.updateOrderStatus(1L, request));
        assertEquals(ErrorCode.ORDER_REJECT_REASON_REQUIRED, ex.getErrorCode());
    }

    @Test
    void getOrderById_valid_shouldReturnDetailResponse() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDetailResponse response = orderService.getOrderById(1L);
        assertEquals(1L, response.orderId());
    }
}
