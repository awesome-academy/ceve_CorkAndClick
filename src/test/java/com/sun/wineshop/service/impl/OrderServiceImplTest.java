package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderSummaryResponse;
import com.sun.wineshop.dto.websocket.OrderNotification;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.model.entity.*;
import com.sun.wineshop.model.enums.OrderStatus;
import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.OrderRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.utils.AppConstants;
import com.sun.wineshop.utils.MessageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MessageUtil messageUtil;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Long userId;
    private Product product;
    private CartItem cartItem;
    private Cart cart;
    private PlaceOrderRequest placeOrderRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "messagingTemplate", messagingTemplate);

        userId = 1L;

        product = new Product();
        product.setId(10L);
        product.setPrice(100.0);

        cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        placeOrderRequest = new PlaceOrderRequest("Nam", "16 Ly Thuong Kiet", "0123456789");

        order = new Order();
        order.setId(1L);
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(100.0);
    }

    @Test
    void placeOrder_shouldPlaceOrderSuccessfully() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productRepository.existsById(product.getId())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        var response = orderService.placeOrder(userId, placeOrderRequest);

        assertThat(response.totalAmount()).isEqualTo(product.getPrice() * cartItem.getQuantity());
        assertThat(response.items()).hasSize(1);
        assertThat(cart.getItems()).isEmpty();
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void placeOrder_shouldThrowWhenCartEmpty() {
        Cart emptyCart = new Cart();
        emptyCart.setUserId(userId);
        emptyCart.setItems(new ArrayList<>());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));
        assertThrows(AppException.class, () -> orderService.placeOrder(userId, placeOrderRequest));
    }

    @Test
    void show_shouldReturnOrderDetailResponse() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        OrderDetailResponse response = orderService.show(order.getId(), userId);
        assertThat(response.orderId()).isEqualTo(order.getId());
    }

    @Test
    void show_shouldThrowWhenUnauthorized() {
        Order otherUserOrder = new Order();
        otherUserOrder.setId(1L);
        otherUserOrder.setUserId(2L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(otherUserOrder));
        assertThrows(AppException.class, () -> orderService.show(1L, userId));
    }

    @Test
    void getOrderHistory_shouldReturnPagedResult() {
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(page);
        Page<OrderSummaryResponse> result = orderService.getOrderHistory(userId, 0, 10);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void cancelOrder_shouldCancelSuccessfully() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.cancelOrder(order.getId(), userId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldThrowIfNotPending() {
        Order deliveringOrder = new Order();
        deliveringOrder.setId(1L);
        deliveringOrder.setUserId(userId);
        deliveringOrder.setStatus(OrderStatus.DELIVERING);

        when(orderRepository.findById(deliveringOrder.getId())).thenReturn(Optional.of(deliveringOrder));
        assertThrows(AppException.class, () -> orderService.cancelOrder(deliveringOrder.getId(), userId));
    }

    @Test
    void updateOrderStatus_shouldUpdateStatusAndSendNotification() {
        Long orderId = order.getId();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(messageUtil.getMessage("order.status.delivering")).thenReturn("Your order is delivering!");

        orderService.updateOrderStatus(orderId, OrderStatus.DELIVERING, userId);
        assertEquals(OrderStatus.DELIVERING, order.getStatus());
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OrderNotification> notificationCaptor = ArgumentCaptor.forClass(OrderNotification.class);

        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), notificationCaptor.capture());
        assertEquals(AppConstants.WS_ORDER_TOPIC + userId, destinationCaptor.getValue());
        OrderNotification notif = notificationCaptor.getValue();
        assertEquals(orderId, notif.orderId());
        assertEquals(OrderStatus.DELIVERING.name(), notif.status());
        assertEquals(order.getTotalAmount(), notif.totalAmount());
        assertEquals("Your order is delivering!", notif.message());
    }
}
