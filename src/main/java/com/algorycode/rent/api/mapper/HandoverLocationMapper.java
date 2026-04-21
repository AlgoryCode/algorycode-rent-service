package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.HandoverLocationDto;
import com.algorycode.rent.api.dto.HandoverLocationRefDto;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.service.readmodel.FeHandoverSnapshotJson;

import java.math.BigDecimal;

public final class HandoverLocationMapper {

  private HandoverLocationMapper() {}

  public static HandoverLocationDto toDto(HandoverLocation e) {
    CityMeta u = cityMeta(e.getCity());
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
        e.getLineOrder(),
        e.getSurchargeEur() != null ? e.getSurchargeEur() : BigDecimal.ZERO,
        e.getFeHandoverSnapshot() != null ? e.getFeHandoverSnapshot() : FeHandoverSnapshotJson.forRow(e));
  }

  public static HandoverLocationRefDto toRef(HandoverLocation e) {
    if (e == null) {
      return null;
    }
    CityMeta u = cityMeta(e.getCity());
    return new HandoverLocationRefDto(
        e.getId(),
        e.getKind(),
        e.getName(),
        e.getDescription(),
        e.getAddressLine(),
        u.cityId,
        u.cityName,
        u.countryCode,
        e.getSurchargeEur() != null ? e.getSurchargeEur() : BigDecimal.ZERO);
  }

  private record CityMeta(Long cityId, String cityName, String countryCode) {}

  private static CityMeta cityMeta(City city) {
    if (city == null) {
      return new CityMeta(null, null, null);
    }
    String countryCode =
        city.getCountry() != null ? city.getCountry().getCode() : null;
    return new CityMeta(city.getId(), city.getName(), countryCode);
  }
}
