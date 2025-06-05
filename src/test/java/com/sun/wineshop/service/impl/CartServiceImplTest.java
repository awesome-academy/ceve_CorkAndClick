package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.request.AddToCartRequest;
import com.sun.wineshop.dto.request.RemoveCartItemRequest;
import com.sun.wineshop.dto.request.UpdateCartItemRequest;
import com.sun.wineshop.dto.response.CartResponse;
import com.sun.wineshop.exception.AppException;
import com.sun.wineshop.exception.ErrorCode;
import com.sun.wineshop.model.entity.Cart;
import com.sun.wineshop.model.entity.CartItem;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.model.entity.User;
import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        product = Product.builder().id(10L).name("Wine").price(100.0).imageUrl("url").build();
        CartItem cartItem = CartItem.builder().product(product).quantity(2).build();

        cart = new Cart();
        cart.setId(100L);
        cart.setUserId(user.getId());
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        cartItem.setCart(cart);
    }

    @Test
    void addToCart_shouldAddNewItem() {
        AddToCartRequest request = new AddToCartRequest(product.getId(), 3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cart.setItems(new ArrayList<>());
        cartService.addToCart(1L, request);

        verify(cartRepository).save(any(Cart.class));
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().getFirst().getQuantity());
    }

    @Test
    void addToCart_shouldUpdateExistingItemQuantity() {
        AddToCartRequest request = new AddToCartRequest(product.getId(), 3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.addToCart(1L, request);

        assertEquals(5, cart.getItems().getFirst().getQuantity());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getCartByUserId_shouldReturnCartResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCartByUserId(1L);

        assertEquals(1L, response.userId());
        assertEquals(1, response.items().size());
        assertEquals(200.0, response.totalAmount());
    }

    @Test
    void updateCartItemQuantity_shouldUpdateExistingItem() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(product.getId(), 5);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.updateCartItemQuantity(1L, request);

        assertEquals(5, cart.getItems().getFirst().getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateCartItemQuantity_shouldRemoveItemIfQuantityZero() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(product.getId(), 0);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.updateCartItemQuantity(1L, request);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void updateCartItemQuantity_shouldAddItemIfNotExistsAndQuantityPositive() {
        UpdateCartItemRequest request = new UpdateCartItemRequest(product.getId(), 2);

        cart.setItems(new ArrayList<>()); // empty cart
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        cartService.updateCartItemQuantity(1L, request);

        assertEquals(1, cart.getItems().size());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemFromCart_shouldRemoveSuccessfully() {
        RemoveCartItemRequest request = new RemoveCartItemRequest(product.getId());

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.removeItemFromCart(1L, request);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemFromCart_shouldThrowIfProductNotFound() {
        RemoveCartItemRequest request = new RemoveCartItemRequest(999L); // wrong product ID

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        AppException exception = assertThrows(AppException.class,
                () -> cartService.removeItemFromCart(1L, request));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND_IN_CART, exception.getErrorCode());
    }
}
