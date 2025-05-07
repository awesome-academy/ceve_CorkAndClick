package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.AddToCartRequest;
import com.sun.wineshop.dto.request.RemoveCartItemRequest;
import com.sun.wineshop.dto.request.UpdateCartItemRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.CartResponse;
import com.sun.wineshop.service.CartService;
import com.sun.wineshop.utils.api.CartApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(CartApiPaths.BASE)
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping(CartApiPaths.Endpoint.ADD)
    public ResponseEntity<BaseApiResponse<String>> addToCart(@RequestBody AddToCartRequest request) {
        cartService.addToCart(request);
        BaseApiResponse<String> response = new BaseApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Product added to cart successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BaseApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        CartResponse cart = cartService.getCartByUserId(userId);
        BaseApiResponse<CartResponse> response = new BaseApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setData(cart);
        response.setMessage("Cart fetched successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping(CartApiPaths.Endpoint.UPDATE_QUANTITY)
    public ResponseEntity<BaseApiResponse<String>> updateQuantity(@RequestBody UpdateCartItemRequest request) {
        cartService.updateCartItemQuantity(request);
        BaseApiResponse<String> response = new BaseApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Updated cart item successfully");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping(CartApiPaths.Endpoint.REMOVE_ITEM)
    public ResponseEntity<BaseApiResponse<String>> removeItem(@RequestBody RemoveCartItemRequest request) {
        cartService.removeItemFromCart(request);
        BaseApiResponse<String> response = new BaseApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Removed item from cart");

        return ResponseEntity.ok(response);
    }
}
