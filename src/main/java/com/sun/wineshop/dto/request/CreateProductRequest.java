package com.sun.wineshop.dto.request;

import com.sun.wineshop.validator.ValidProductName;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateProductRequest(

        @ValidProductName
        String name,

        @Size(max = 500, message = "PRODUCT_DESCRIPTION_INVALID")
        String description,

        @NotBlank(message = "PRODUCT_IMAGE_URL_INVALID")
        @Size(max = 255, message = "PRODUCT_IMAGE_URL_INVALID")
        String imageUrl,

        @NotNull(message = "PRODUCT_PRICE_INVALID")
        @DecimalMin(value = "0.0", inclusive = false, message = "PRODUCT_PRICE_INVALID")
        Double price,

        @NotBlank(message = "PRODUCT_ORIGIN_INVALID")
        @Size(max = 100, message = "PRODUCT_ORIGIN_INVALID")
        String origin,

        @NotNull(message = "PRODUCT_VOLUME_INVALID")
        @Min(value = 50, message = "PRODUCT_VOLUME_INVALID")
        Integer volume,

        @NotNull(message = "PRODUCT_STOCK_INVALID")
        @Min(value = 0, message = "PRODUCT_STOCK_INVALID")
        Integer stockQuantity,

        @NotNull(message = "PRODUCT_ALCOHOL_PERCENTAGE_INVALID")
        @DecimalMin(value = "0.0", inclusive = true, message = "PRODUCT_ALCOHOL_PERCENTAGE_INVALID")
        @DecimalMax(value = "100.0", inclusive = true, message = "PRODUCT_ALCOHOL_PERCENTAGE_INVALID")
        Double alcoholPercentage,

        @NotEmpty(message = "PRODUCT_CATEGORIES_REQUIRED")
        List<@NotNull(message = "PRODUCT_CATEGORY_ID_NULL") Long> categoryIds
) {}
