package com.sun.wineshop.controller.admin;

import com.sun.wineshop.dto.request.CategoryRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.dto.response.CategoryResponse;
import com.sun.wineshop.service.CategoryService;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.AdminApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController(AdminApiPaths.Category.ADMIN_CATEGORY_CONTROLLER)
@RequiredArgsConstructor
@RequestMapping(AdminApiPaths.Category.BASE)
public class CategoryController {

    private final CategoryService categoryService;
    private final MessageUtil messageUtil;

    @PostMapping
    private ResponseEntity<BaseApiResponse<CategoryResponse>> createCategory(@RequestBody CategoryRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new BaseApiResponse<>(
                        HttpStatus.OK.value(),
                        categoryService.createCategory(request)
                )
        );
    }

    @PutMapping(AdminApiPaths.Category.BY_ID)
    private ResponseEntity<BaseApiResponse<Void>> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        categoryService.updateCategory(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(
                new BaseApiResponse<>(
                        HttpStatus.OK.value(),
                        messageUtil.getMessage("category.update.success")
                )
        );
    }

    @DeleteMapping(AdminApiPaths.Category.BY_ID)
    private ResponseEntity<BaseApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);

        return ResponseEntity.status(HttpStatus.OK).body(
                new BaseApiResponse<>(
                        HttpStatus.OK.value(),
                        messageUtil.getMessage("category.delete.success")
                )
        );
    }
}
