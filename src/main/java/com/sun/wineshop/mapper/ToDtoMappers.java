package com.sun.wineshop.mapper;

import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.dto.response.OrderItemResponse;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.dto.response.UserResponse;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.OrderItem;
import com.sun.wineshop.model.entity.Product;
import com.sun.wineshop.model.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class ToDtoMappers {

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getBirthday(),
                user.getRole()
        );
    }

    public static ProductResponse toProductResponse(Product product) {
        List<CategoryResponse> categoryResponses = product.getCategories().stream()
                .map(ToDtoMappers::toCategoryResponse)
                .collect(Collectors.toList());

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getOrigin(),
                product.getVolume(),
                product.getStockQuantity(),
                product.getAlcoholPercentage(),
                categoryResponses,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    public static OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice()
        );
    }
}
