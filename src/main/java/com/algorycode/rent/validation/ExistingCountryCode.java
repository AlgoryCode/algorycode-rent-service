package com.algorycode.rent.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Ülkeler tablosunda (büyük/küçük harf duyarsız) kayıtlı bir ülke kodu olmalıdır. */
@Documented
@Constraint(validatedBy = ExistingCountryCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistingCountryCode {

  String message() default "{ExistingCountryCode.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
