package com.sun.wineshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderItemResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.dto.response.OrderSummaryResponse;
import com.sun.wineshop.service.OrderService;
import com.sun.wineshop.utils.JwtUtil;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.OrderApiPaths;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }

        @Bean
        public MessageUtil messageUtil() {
            MessageUtil messageUtil = Mockito.mock(MessageUtil.class);
            Mockito.when(messageUtil.getMessage("order.placed.success")).thenReturn("Order placed successfully");
            Mockito.when(messageUtil.getMessage("order.detail.fetched.success")).thenReturn("Order detail fetched successfully");
            Mockito.when(messageUtil.getMessage("order.history.fetched.success")).thenReturn("Order history fetched successfully");
            Mockito.when(messageUtil.getMessage("order.cancel.success")).thenReturn("Order cancelled successfully");
            return messageUtil;
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private OrderService orderService;
    @Autowired private MessageUtil messageUtil;

    private Jwt jwt;
    private static final Long USER_ID = 1L;
    private MockedStatic<JwtUtil> jwtUtilMockedStatic;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        jwt = Mockito.mock(Jwt.class);
        jwtUtilMockedStatic = Mockito.mockStatic(JwtUtil.class);
        jwtUtilMockedStatic.when(() -> JwtUtil.extractUserIdFromJwt(jwt)).thenReturn(USER_ID);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        jwtUtilMockedStatic.close();
        Mockito.reset(orderService);
    }

    @Test
    void placeOrder_success() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest("Nam", "16 Ly Thuong Kiet", "0123456789");

        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(1L, "Wine A", 2, 50.0),
                new OrderItemResponse(2L, "Wine B", 1, 70.0)
        );
        OrderResponse response = new OrderResponse(100L, 170.0, "PENDING", items);

        Mockito.when(orderService.placeOrder(USER_ID, request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post(OrderApiPaths.BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(jwt().jwt(jwt))
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.data.orderId").value(100L))
                .andExpect(jsonPath("$.data.totalAmount").value(170.0))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        Mockito.verify(orderService).placeOrder(USER_ID, request);
    }

    @Test
    void getOrderDetail_success() throws Exception {
        Long orderId = 99L;
        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(1L, "Wine A", 3, 150.0)
        );

        OrderDetailResponse detailResponse = new OrderDetailResponse(
                orderId,
                USER_ID,
                "Nam 1",
                "16 Ly Thuong Kiet",
                "0987654321",
                "DELIVERED",
                150.0,
                LocalDateTime.now(),
                items
        );

        Mockito.when(orderService.show(orderId, USER_ID)).thenReturn(detailResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(OrderApiPaths.BASE + "/" + orderId)
                        .with(jwt().jwt(jwt))
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order detail fetched successfully"))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));

        Mockito.verify(orderService).show(orderId, USER_ID);
    }

    @Test
    void getOrderHistory_success() throws Exception {
        OrderSummaryResponse summary = new OrderSummaryResponse(
                101L,
                200.0,
                "DELIVERED",
                LocalDateTime.now()
        );
        Page<OrderSummaryResponse> page = new PageImpl<>(List.of(summary));

        Mockito.when(orderService.getOrderHistory(USER_ID, 0, 10)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(OrderApiPaths.BASE)
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .with(jwt().jwt(jwt))
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order history fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].orderId").value(101L))
                .andExpect(jsonPath("$.data.content[0].status").value("DELIVERED"));

        Mockito.verify(orderService).getOrderHistory(USER_ID, 0, 10);
    }

    @Test
    void cancelOrder_success() throws Exception {
        Long orderId = 123L;

        mockMvc.perform(MockMvcRequestBuilders.put(OrderApiPaths.BASE + OrderApiPaths.Endpoint.CANCEL, orderId)
                        .with(jwt().jwt(jwt))
                        .with(csrf())
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));

        Mockito.verify(orderService).cancelOrder(orderId, USER_ID);
    }
}
