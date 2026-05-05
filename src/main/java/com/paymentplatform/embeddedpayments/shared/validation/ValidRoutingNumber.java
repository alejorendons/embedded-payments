package com.paymentplatform.embeddedpayments.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RoutingNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoutingNumber {
    String message() default "Invalid routing number format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

