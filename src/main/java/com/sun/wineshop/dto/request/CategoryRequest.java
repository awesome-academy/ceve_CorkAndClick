package com.sun.wineshop.dto.request;

import com.sun.wineshop.validator.ValidCategoryName;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CategoryRequest(
        @ValidCategoryName
        String name,
        @Size(max = 255, message = "CATEGORY_DESCRIPTION_INVALID")
        String description
) {
}
