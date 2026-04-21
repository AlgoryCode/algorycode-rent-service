package com.algorycode.rent.validation;

import com.algorycode.rent.repository.CountryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class ExistingCountryCodeValidator implements ConstraintValidator<ExistingCountryCode, String> {

  private final CountryRepository countryRepository;

  public ExistingCountryCodeValidator(CountryRepository countryRepository) {
    this.countryRepository = countryRepository;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }
    String normalized = value.trim().toUpperCase();
    return countryRepository.findByCodeIgnoreCase(normalized).isPresent();
  }
}
