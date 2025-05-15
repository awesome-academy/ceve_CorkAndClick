package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.AddToCartRequest;
import com.sun.wineshop.dto.request.RemoveCartItemRequest;
import com.sun.wineshop.dto.request.UpdateCartItemRequest;
import com.sun.wineshop.dto.response.CartResponse;

public interface CartService {
    void addToCart(Long userId, AddToCartRequest request);
    CartResponse getCartByUserId(Long userId);
    void updateCartItemQuantity(Long userId, UpdateCartItemRequest request);
    void removeItemFromCart(Long userId, RemoveCartItemRequest request);
}
