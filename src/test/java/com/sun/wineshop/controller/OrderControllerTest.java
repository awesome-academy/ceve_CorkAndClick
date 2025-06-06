package com.sun.wineshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.wineshop.configuration.CustomJwtDecoder;
import com.sun.wineshop.configuration.SecurityConfig;
import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderItemResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.dto.response.OrderSummaryResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.exception.GlobalExceptionHandler;
import com.sun.wineshop.service.OrderService;
import com.sun.wineshop.utils.AppConstants;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.OrderApiPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({
        OrderControllerTest.TestConfig.class,
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public MessageSource messageSource() {
            ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
            source.setBasename("classpath:messages");
            source.setDefaultEncoding("UTF-8");
            return source;
        }

        @Bean
        public MessageUtil messageUtil(MessageSource source) {
            return new MessageUtil(source);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @Autowired
    private MessageUtil messageUtil;

    private void setSecurityContextWithClaims(Map<String, Object> claims) {
        SecurityContextHolder.clearContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Jwt jwt = new Jwt(
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims
        );
        context.setAuthentication(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.setContext(context);
    }

    private void setDefaultUserSecurityContext() {
        setSecurityContextWithClaims(Map.of(AppConstants.JWT_USER_ID, 1L));
    }

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void placeOrder_shouldReturnOrderResponse() throws Exception {
        setDefaultUserSecurityContext();
        PlaceOrderRequest request = new PlaceOrderRequest("Minh", "VietNam", "0999999999");
        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(1L, "Red Wine", 2, 300000.0),
                new OrderItemResponse(2L, "White Wine", 1, 150000.0)
        );
        OrderResponse response = new OrderResponse(1L, 100000.0, "PENDING", items);

        when(orderService.placeOrder(eq(1L), any(PlaceOrderRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post(OrderApiPaths.BASE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(1L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void placeOrder_missingUserIdClaim_shouldReturnUnauthorized() throws Exception {
        PlaceOrderRequest request = new PlaceOrderRequest("Minh", "VN", "0909090909");
        setSecurityContextWithClaims(Map.of("sub", "someUser"));
        mockMvc.perform(MockMvcRequestBuilders.post(OrderApiPaths.BASE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.UNAUTHORIZED.getMessageKey())));
    }

    @Test
    void show_shouldReturnOrderDetail() throws Exception {
        setDefaultUserSecurityContext();
        OrderDetailResponse response = new OrderDetailResponse(
                1L, 1L, "John Doe", "123 Street", "0123456789", "PAID", null,
                120000.0, LocalDateTime.now(), List.of()
        );

        when(orderService.show(1L, 1L)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get(OrderApiPaths.BASE + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value(1L))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void show_orderNotFound_shouldReturnNotFound() throws Exception {
        setDefaultUserSecurityContext();
        when(orderService.show(99L, 1L))
                .thenThrow(new AppException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(MockMvcRequestBuilders.get(OrderApiPaths.BASE + "/99"))
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage(ErrorCode.ORDER_NOT_FOUND.getMessageKey())));
    }

    @Test
    void getOrderHistory_shouldReturnPagedOrders() throws Exception {
        setDefaultUserSecurityContext();
        OrderSummaryResponse order1 = new OrderSummaryResponse(1L, 120000.0, "DELIVERED", LocalDateTime.now());
        OrderSummaryResponse order2 = new OrderSummaryResponse(2L, 90000.0, "CANCELLED", LocalDateTime.now());
        Page<OrderSummaryResponse> page = new PageImpl<>(List.of(order1, order2));

        when(orderService.getOrderHistory(1L, 0, 10)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get(OrderApiPaths.BASE)
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].orderId").value(1L))
                .andExpect(jsonPath("$.data.content[0].status").value("DELIVERED"))
                .andExpect(jsonPath("$.data.content[1].orderId").value(2L))
                .andExpect(jsonPath("$.data.content[1].status").value("CANCELLED"));
    }

    @Test
    void cancelOrder_shouldReturnSuccess() throws Exception {
        setDefaultUserSecurityContext();
        mockMvc.perform(MockMvcRequestBuilders.put(OrderApiPaths.BASE + "/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(messageUtil.getMessage("order.cancel.success")));

        verify(orderService).cancelOrder(1L, 1L);
    }

    private Jwt mockJwt(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AppConstants.JWT_USER_ID, userId);
        return new Jwt(
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "none"),
                claims
        );
    }

    private Authentication mockJwtPrincipal(Long userId) {
        return new JwtAuthenticationToken(mockJwt(userId));
    }
}
