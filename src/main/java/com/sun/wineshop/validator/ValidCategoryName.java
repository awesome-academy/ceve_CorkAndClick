package com.sun.wineshop.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CategoryNameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCategoryName {

    String message() default "CATEGORY_NAME_INVALID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
