package com.algorycode.rent.validation;

import com.algorycode.rent.api.dto.CreateVehicleRequest;
import com.algorycode.rent.repository.CityRepository;
import com.algorycode.rent.repository.CountryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class VehicleCityMatchesCountryValidator
    implements ConstraintValidator<VehicleCityMatchesCountry, CreateVehicleRequest> {

  private final CountryRepository countryRepository;
  private final CityRepository cityRepository;

  public VehicleCityMatchesCountryValidator(
      CountryRepository countryRepository, CityRepository cityRepository) {
    this.countryRepository = countryRepository;
    this.cityRepository = cityRepository;
  }

  @Override
  public boolean isValid(CreateVehicleRequest value, ConstraintValidatorContext context) {
    if (value == null || value.cityId() == null) {
      return true;
    }
    String rawCode = value.countryCode();
    if (rawCode == null || rawCode.isBlank()) {
      return true;
    }
    Long countryId =
        countryRepository
            .findByCodeIgnoreCase(rawCode.trim().toUpperCase())
            .map(c -> c.getId())
            .orElse(null);
    if (countryId == null) {
      return true;
    }
    var cityOpt = cityRepository.findById(value.cityId());
    if (cityOpt.isEmpty()) {
      addViolation(context, "Şehir bulunamadı: " + value.cityId());
      return false;
    }
    var city = cityOpt.get();
    if (Objects.equals(city.getCountryId(), countryId)) {
      return true;
    }
    String template = context.getDefaultConstraintMessageTemplate();
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(template).addPropertyNode("cityId").addConstraintViolation();
    return false;
  }

  private static void addViolation(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addPropertyNode("cityId").addConstraintViolation();
  }
}
