package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.CityDto;
import com.algorycode.rent.domain.location.City;

public final class CityMapper {

  private CityMapper() {}

  public static CityDto toDto(City city) {
    var c = city.getCountry();
    return new CityDto(
        city.getId(),
        city.getName(),
        c != null ? c.getId() : null,
        c != null ? c.getCode() : null,
        c != null ? c.getName() : null);
  }
}
