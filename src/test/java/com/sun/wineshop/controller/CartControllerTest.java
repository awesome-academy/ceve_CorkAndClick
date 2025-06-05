package com.sun.wineshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.wineshop.dto.request.AddToCartRequest;
import com.sun.wineshop.dto.request.RemoveCartItemRequest;
import com.sun.wineshop.dto.request.UpdateCartItemRequest;
import com.sun.wineshop.dto.response.CartItemResponse;
import com.sun.wineshop.dto.response.CartResponse;
import com.sun.wineshop.service.CartService;
import com.sun.wineshop.utils.JwtUtil;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.CartApiPaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        public CartService cartService() {
            return Mockito.mock(CartService.class);
        }

        @Bean
        public MessageUtil messageUtil() {
            MessageUtil messageUtil = Mockito.mock(MessageUtil.class);
            Mockito.when(messageUtil.getMessage("cart.add.product.success")).thenReturn("Add product success");
            Mockito.when(messageUtil.getMessage("cart.fetched.success")).thenReturn("Cart fetched success");
            Mockito.when(messageUtil.getMessage("cart.update.success")).thenReturn("Cart update success");
            Mockito.when(messageUtil.getMessage("cart.remove.item.success")).thenReturn("Remove item success");
            return messageUtil;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartService cartService;

    @Autowired
    private MessageUtil messageUtil;

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
        Mockito.reset(cartService);
    }

    @Test
    void addToCart_success() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(10L)
                .quantity(2)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post(CartApiPaths.BASE + CartApiPaths.Endpoint.ADD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf())
                        .with(jwt().jwt(jwt))
                        .header("Authorization", "Bearer dummy-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Add product success"));

        Mockito.verify(cartService).addToCart(USER_ID, request);
    }

    @Test
    void showCart_success() throws Exception {
        CartItemResponse item = CartItemResponse.builder()
                .productId(10L)
                .productName("Wine A")
                .quantity(2)
                .price(100.0)
                .build();
        CartResponse cartResponse = CartResponse.builder()
                .cartId(1L)
                .userId(1L)
                .items(List.of(item))
                .totalAmount(200.0)
                .build();

        Mockito.when(cartService.getCartByUserId(USER_ID)).thenReturn(cartResponse);

        mockMvc.perform(MockMvcRequestBuilders.get(CartApiPaths.BASE)
                        .with(jwt().jwt(jwt))
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.message").value("Cart fetched success"));

        Mockito.verify(cartService).getCartByUserId(USER_ID);
    }

    @Test
    void updateQuantity_success() throws Exception {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .productId(10L)
                .quantity(5)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.put(CartApiPaths.BASE + CartApiPaths.Endpoint.UPDATE_QUANTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf())
                        .with(jwt().jwt(jwt))
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cart update success"));

        Mockito.verify(cartService).updateCartItemQuantity(USER_ID, request);
    }

    @Test
    void removeItem_success() throws Exception {
        RemoveCartItemRequest request = RemoveCartItemRequest.builder()
                .productId(10L)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.delete(CartApiPaths.BASE + CartApiPaths.Endpoint.REMOVE_ITEM)
                        .with(csrf())
                        .with(jwt().jwt(jwt))
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Remove item success"))
                .andExpect(jsonPath("$.data").doesNotExist());

        Mockito.verify(cartService).removeItemFromCart(USER_ID, request);
    }

}
