package com.sun.wineshop.controller.admin;

import com.sun.wineshop.dto.request.UpdateOrderStatusRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.service.OrderService;
import com.sun.wineshop.utils.JwtUtil;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController(AdminApiPaths.Order.ADMIN_ORDER_CONTROLLER)
@RequiredArgsConstructor
@RequestMapping(AdminApiPaths.Order.BASE)
public class OrderController {

    private final OrderService orderService;
    private final MessageUtil messageUtil;

    @PutMapping(AdminApiPaths.Order.UPDATE_STATUS)
    public ResponseEntity<BaseApiResponse<Void>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = JwtUtil.extractUserIdFromJwt(jwt);
        orderService.updateOrderStatus(orderId, request.status(), userId);

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                messageUtil.getMessage("order.status.update.success")
        ));
    }
}
