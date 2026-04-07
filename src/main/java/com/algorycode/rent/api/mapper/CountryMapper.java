package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.CountryDto;
import com.algorycode.rent.domain.country.Country;

public final class CountryMapper {

  private CountryMapper() {}

  public static CountryDto toDto(Country c) {
    return new CountryDto(c.getId(), c.getCode(), c.getName(), c.getColorCode());
  }
}
