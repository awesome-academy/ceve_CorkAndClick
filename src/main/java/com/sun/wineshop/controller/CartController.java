package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.AddToCartRequest;
import com.sun.wineshop.dto.request.RemoveCartItemRequest;
import com.sun.wineshop.dto.request.UpdateCartItemRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.CartResponse;
import com.sun.wineshop.service.CartService;
import com.sun.wineshop.utils.AppConstants;
import com.sun.wineshop.utils.JwtUtil;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.CartApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(CartApiPaths.BASE)
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final MessageUtil messageUtil;

    @PostMapping(CartApiPaths.Endpoint.ADD)
    public ResponseEntity<BaseApiResponse<String>> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AddToCartRequest request) {
        cartService.addToCart(JwtUtil.extractUserIdFromJwt(jwt), request);

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                messageUtil.getMessage("cart.add.product.success")
        ));
    }

    @GetMapping
    public ResponseEntity<BaseApiResponse<CartResponse>> show(@AuthenticationPrincipal Jwt jwt) {
        CartResponse cart = cartService.getCartByUserId(JwtUtil.extractUserIdFromJwt(jwt));

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                cart,
                messageUtil.getMessage("cart.fetched.success")
        ));
    }

    @PutMapping(CartApiPaths.Endpoint.UPDATE_QUANTITY)
    public ResponseEntity<BaseApiResponse<String>> updateQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateCartItemRequest request) {
        cartService.updateCartItemQuantity(JwtUtil.extractUserIdFromJwt(jwt), request);

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                messageUtil.getMessage("cart.update.success")
        ));
    }

    @DeleteMapping(CartApiPaths.Endpoint.REMOVE_ITEM)
    public ResponseEntity<BaseApiResponse<String>> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RemoveCartItemRequest request) {
        cartService.removeItemFromCart(JwtUtil.extractUserIdFromJwt(jwt), request);

        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                messageUtil.getMessage("cart.remove.item.success")
        ));
    }
}
