package com.sun.wineshop.service;

import com.sun.wineshop.dto.request.AddToCartRequest;
import com.sun.wineshop.dto.request.RemoveCartItemRequest;
import com.sun.wineshop.dto.request.UpdateCartItemRequest;
import com.sun.wineshop.dto.response.CartResponse;

public interface CartService {
    void addToCart(AddToCartRequest request);
    CartResponse getCartByUserId(Long userId);
    void updateCartItemQuantity(UpdateCartItemRequest request);
    void removeItemFromCart(RemoveCartItemRequest request);
}
