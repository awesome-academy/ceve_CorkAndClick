package com.sun.wineshop.service;

import com.sun.wineshop.repository.CartRepository;
import com.sun.wineshop.repository.ProductRepository;
import com.sun.wineshop.repository.UserRepository;

public abstract class BaseService {
    protected final UserRepository userRepository;
    protected final ProductRepository productRepository;
    protected final CartRepository cartRepository;

    protected BaseService(
            UserRepository userRepository,
            ProductRepository productRepository,
            CartRepository cartRepository
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
    }

    protected BaseService(ProductRepository productRepository) {
        this(null, productRepository, null);
    }
}
