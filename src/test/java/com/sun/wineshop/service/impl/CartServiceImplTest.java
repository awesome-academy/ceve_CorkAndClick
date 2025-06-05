package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.AddToCartRequest;
import com.sun.wineshop.dto.request.RemoveCartItemRequest;
import com.sun.wineshop.dto.request.UpdateCartItemRequest;
import com.sun.wineshop.dto.response.CartItemResponse;
import com.sun.wineshop.dto.response.CartResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.model.entity.*;
import com.sun.wineshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Long userId;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = 1L;
        User user = new User();
        user.setId(userId);

        product = new Product();
        product.setId(100L);
        product.setName("Wine");
        product.setPrice(100.0);
        product.setImageUrl("image.jpg");
        product.setStockQuantity(10);

        cart = new Cart();
        cart.setId(10L);
        cart.setUserId(userId);

        cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
    }

    @Test
    void getCartByUserId_shouldReturnCartResponse() {
        CartResponse response = cartService.getCartByUserId(userId);

        assertNotNull(response);
        assertEquals(cart.getId(), response.cartId());
        assertEquals(userId, response.userId());
        assertEquals(1, response.items().size());

        CartItemResponse itemResponse = response.items().getFirst();
        assertEquals(product.getId(), itemResponse.productId());
        assertEquals(product.getName(), itemResponse.productName());
        assertEquals(product.getImageUrl(), itemResponse.imageUrl());
        assertEquals(product.getPrice(), itemResponse.price());
        assertEquals(cartItem.getQuantity(), itemResponse.quantity());
        assertTrue(itemResponse.isAvailable());
        assertEquals(product.getPrice() * cartItem.getQuantity(), response.totalAmount());
    }

    @Test
    void addToCart_shouldAddNewItem() {
        Long productId = 2L;
        int quantity = 3;
        AddToCartRequest request = new AddToCartRequest(productId, quantity);

        Product product2 = new Product();
        product2.setId(productId);
        product2.setName("Wine 2");
        product2.setPrice(150.0);
        product2.setStockQuantity(10);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.of(product2));
        cartService.addToCart(userId, request);
        verify(cartRepository).save(cart);
    }

    @Test
    void addToCart_shouldThrowIfProductNotFound() {
        Long productId = 2L;
        int quantity = 1;
        AddToCartRequest request = new AddToCartRequest(productId, quantity);

        when(productRepository.findByIdAndDeletedAtIsNull(productId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> cartService.addToCart(userId, request));
    }

    @Test
    void updateCartItemQuantity_shouldUpdateQuantity() {
        Long productId = product.getId();
        int newQuantity = 5;
        UpdateCartItemRequest request = new UpdateCartItemRequest(productId, newQuantity);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        cartService.updateCartItemQuantity(userId, request);
        assertEquals(newQuantity, cartItem.getQuantity());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateCartItemQuantity_shouldThrowIfProductNotFound() {
        Long productId = 2L;
        int newQuantity = 3;
        UpdateCartItemRequest request = new UpdateCartItemRequest(productId, newQuantity);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> cartService.updateCartItemQuantity(userId, request));
    }

    @Test
    void updateCartItemQuantity_shouldThrowIfCartNotFound() {
        Long productId = 2L;
        int newQuantity = 3;
        UpdateCartItemRequest request = new UpdateCartItemRequest(productId, newQuantity);

        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> cartService.updateCartItemQuantity(userId, request));
    }

    @Test
    void removeItemFromCart_shouldRemoveItemSuccessfully() {
        RemoveCartItemRequest request = new RemoveCartItemRequest(product.getId());

        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.removeItemFromCart(userId, request);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemFromCart_shouldThrowIfCartNotFound() {
        RemoveCartItemRequest request = new RemoveCartItemRequest(product.getId());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> cartService.removeItemFromCart(userId, request));
    }

    @Test
    void removeItemFromCart_shouldThrowIfProductNotInCart() {
        Long notInCartProductId = 999L;
        RemoveCartItemRequest request = new RemoveCartItemRequest(notInCartProductId);

        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        assertThrows(AppException.class, () -> cartService.removeItemFromCart(userId, request));
    }
}
