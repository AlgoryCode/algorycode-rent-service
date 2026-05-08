package com.algorycode.rent.mapper;

import com.algorycode.rent.dto.CityDto;
import com.algorycode.rent.entity.City;

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
