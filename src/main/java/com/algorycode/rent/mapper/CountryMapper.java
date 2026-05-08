package com.algorycode.rent.mapper;

import com.algorycode.rent.dto.CountryDto;
import com.algorycode.rent.entity.Country;

public final class CountryMapper {

  private CountryMapper() {}

  public static CountryDto toDto(Country c) {
    return new CountryDto(c.getId(), c.getCode(), c.getName(), c.getColorCode());
  }
}
