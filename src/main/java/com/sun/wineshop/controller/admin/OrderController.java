package com.sun.wineshop.controller.admin;

import com.sun.wineshop.dto.request.UpdateOrderStatusRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.OrderDetailResponse;
import com.sun.wineshop.model.enums.OrderStatus;
import com.sun.wineshop.service.OrderService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController(AdminApiPaths.Order.ADMIN_ORDER_CONTROLLER)
@RequiredArgsConstructor
@RequestMapping(AdminApiPaths.Order.BASE)
public class OrderController {

    private final OrderService orderService;
    private final MessageUtil messageUtil;

    @GetMapping
    public ResponseEntity<BaseApiResponse<Page<OrderDetailResponse>>> getOrders(
            @RequestParam(value = "status", required = false) OrderStatus status,
            Pageable pageable) {

        Page<OrderDetailResponse> orders = orderService.getOrders(status, pageable);

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                orders,
                messageUtil.getMessage("orders.fetched.success")
        ));
    }

    @GetMapping(AdminApiPaths.Order.BY_ID)
    public ResponseEntity<BaseApiResponse<OrderDetailResponse>> getOrderById(@PathVariable Long id) {

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                orderService.getOrderById(id)
        ));
    }

    @PutMapping(AdminApiPaths.Order.UPDATE_STATUS)
    public ResponseEntity<BaseApiResponse<OrderDetailResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderStatusRequest request
    ) {

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                orderService.updateOrderStatus(id, request)
        ));
    }
}
