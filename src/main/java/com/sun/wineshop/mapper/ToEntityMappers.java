package com.sun.wineshop.mapper;

import com.sun.wineshop.dto.request.ProductRequest;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.Product;

import java.util.List;

public class ToEntityMappers {

    public static Product toProduct(ProductRequest request, List<Category> categories) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .price(request.price())
                .origin(request.origin())
                .volume(request.volume())
                .stockQuantity(request.stockQuantity())
                .alcoholPercentage(request.alcoholPercentage())
                .categories(categories)
                .build();
    }
}
