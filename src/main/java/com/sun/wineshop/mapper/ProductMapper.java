package com.sun.wineshop.mapper;

import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.dto.response.ProductResponse;
import com.sun.wineshop.model.entity.Category;
import com.sun.wineshop.model.entity.Product;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductResponse toProductResponse(Product product) {
        List<CategoryResponse> categoryResponses = product.getCategories().stream()
                .map(ProductMapper::toCategoryResponse)
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

    private static CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
