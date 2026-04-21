package com.algorycode.rent.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link com.algorycode.rent.api.dto.CreateVehicleRequest} için: {@code cityId} doluysa şehir mevcut olmalı ve
 * {@code countryCode} ile aynı ülkeye bağlı olmalıdır.
 */
@Documented
@Constraint(validatedBy = VehicleCityMatchesCountryValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface VehicleCityMatchesCountry {

  String message() default "{VehicleCityMatchesCountry.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
