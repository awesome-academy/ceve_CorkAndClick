package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.dto.response.OrderSummaryResponse;
import com.sun.wineshop.service.OrderService;
import com.sun.wineshop.utils.AppConstants;
import com.sun.wineshop.utils.JwtUtil;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.OrderApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(OrderApiPaths.BASE)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MessageUtil messageUtil;

    @PostMapping
    public ResponseEntity<BaseApiResponse<OrderResponse>> placeOrder(@RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                response,
                messageUtil.getMessage("order.placed.success")
        ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<BaseApiResponse<OrderDetailResponse>> show(
            @PathVariable Long orderId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = JwtUtil.extractUserIdFromJwt(jwt);
        OrderDetailResponse response = orderService.show(orderId, userId);
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                response,
                messageUtil.getMessage("order.detail.fetched.success")
        ));
    }

    @GetMapping(OrderApiPaths.Endpoint.HISTORY)
    public ResponseEntity<BaseApiResponse<List<OrderSummaryResponse>>> getOrderHistory(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = JwtUtil.extractUserIdFromJwt(jwt);
        List<OrderSummaryResponse> orders = orderService.getOrderHistory(userId);

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                orders,
                messageUtil.getMessage("order.history.fetched.success")
        ));
    }
}
