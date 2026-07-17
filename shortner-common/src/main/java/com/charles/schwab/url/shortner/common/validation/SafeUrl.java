package com.charles.schwab.url.shortner.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SafeUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeUrl {

    String message() default "Invalid or prohibited URL. Internal IP addresses and localhost are not allowed.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
