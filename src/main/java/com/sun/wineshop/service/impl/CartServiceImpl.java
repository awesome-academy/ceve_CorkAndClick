package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.AddToCartRequest;
import com.sun.wineshop.dto.request.RemoveCartItemRequest;
import com.sun.wineshop.dto.request.UpdateCartItemRequest;
import com.sun.wineshop.dto.response.CartItemResponse;
import com.sun.wineshop.dto.response.CartResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Cart;
import com.sun.wineshop.model.entity.CartItem;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.repository.UserRepository;
import com.sun.wineshop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public void addToCart(AddToCartRequest request) {
        var user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        var product = productRepository.findById(request.productId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        var cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    var newCart = new Cart();
                    newCart.setUserId(user.getId());
                    return cartRepository.save(newCart);
                });

        var existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.quantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    @Override
    public CartResponse getCartByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getPrice(),
                        item.getQuantity()
                )).toList();

        double total = itemResponses.stream()
                .mapToDouble(i -> i.price() * i.quantity())
                .sum();

        return new CartResponse(cart.getId(), userId, itemResponses, total);
    }

    @Override
    public void updateCartItemQuantity(UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(request.productId()))
                .findFirst()
                .orElse(null);

        if (item == null) {
            if (request.quantity() > 0) {
                Product product = productRepository.findById(request.productId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(request.quantity())
                        .build();
                cart.getItems().add(newItem);
            }
        } else {
            if (request.quantity() > 0) {
                item.setQuantity(request.quantity());
            } else {
                cart.getItems().remove(item);
            }
        }

        cartRepository.save(cart);
    }

    @Override
    public void removeItemFromCart(RemoveCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        boolean removed = cart.getItems().removeIf(i -> i.getProduct().getId().equals(request.productId()));

        if (removed) {
            cartRepository.save(cart);
        } else {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND_IN_CART);
        }
    }
}
