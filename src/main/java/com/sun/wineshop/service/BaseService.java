package com.sun.wineshop.service;

import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.OrderRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.repository.UserRepository;

public abstract class BaseService {
    protected final UserRepository userRepository;
    protected final ProductRepository productRepository;
    protected final CartRepository cartRepository;
    protected final OrderRepository orderRepository;

    protected BaseService(
            UserRepository userRepository,
            ProductRepository productRepository,
            CartRepository cartRepository,
            OrderRepository orderRepository
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }
}
