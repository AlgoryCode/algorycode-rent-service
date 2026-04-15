package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.HandoverLocationDto;
import com.algorycode.rent.api.dto.HandoverLocationRefDto;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.domain.location.HandoverLocation;

import java.util.UUID;

public final class HandoverLocationMapper {

  private HandoverLocationMapper() {}

  public static HandoverLocationDto toDto(HandoverLocation e) {
    UUIDAndNames u = cityMeta(e.getCity());
    return new HandoverLocationDto(
        e.getId(),
        e.getKind(),
        e.getName(),
        e.getDescription(),
        e.getAddressLine(),
        u.cityId,
        u.cityName,
        u.countryCode,
        e.isActive(),
        e.getLineOrder());
  }

  public static HandoverLocationRefDto toRef(HandoverLocation e) {
    if (e == null) {
      return null;
    }
    UUIDAndNames u = cityMeta(e.getCity());
    return new HandoverLocationRefDto(
        e.getId(),
        e.getKind(),
        e.getName(),
        e.getDescription(),
        e.getAddressLine(),
        u.cityId,
        u.cityName,
        u.countryCode);
  }

  private record UUIDAndNames(UUID cityId, String cityName, String countryCode) {}

  private static UUIDAndNames cityMeta(City city) {
    if (city == null) {
      return new UUIDAndNames(null, null, null);
    }
    String countryCode =
        city.getCountry() != null ? city.getCountry().getCode() : null;
    return new UUIDAndNames(city.getId(), city.getName(), countryCode);
  }
}
