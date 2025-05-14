package com.sun.wineshop.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProductNameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProductName {
    String message() default "PRODUCT_NAME_INVALID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
