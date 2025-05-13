package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.PlaceOrderRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.OrderResponse;
import com.sun.wineshop.service.OrderService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.OrderApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
