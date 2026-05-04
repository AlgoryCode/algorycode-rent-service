package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.HandoverLocationDto;
import com.algorycode.rent.api.dto.HandoverLocationRefDto;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.service.readmodel.FeHandoverSnapshotJson;
import java.math.BigDecimal;

/** Alış/teslim noktası DTO dönüşümleri (şehir FK’si yok; {@code countryCode} satır alanı). */
public final class HandoverLocationMapper {

  private HandoverLocationMapper() {}

  public static HandoverLocationDto toDto(HandoverLocation e) {
    return new HandoverLocationDto(
        e.getId(),
        e.getKind(),
        e.getName(),
        e.getDescription(),
        e.getAddressLine(),
        e.getCountryCode(),
        e.isActive(),
        e.getLineOrder(),
        e.getSurchargeEur() != null ? e.getSurchargeEur() : BigDecimal.ZERO,
        e.getFeHandoverSnapshot() != null
            ? e.getFeHandoverSnapshot()
            : FeHandoverSnapshotJson.forRow(e));
  }

  public static HandoverLocationRefDto toRef(HandoverLocation e) {
    if (e == null) {
      return null;
    }
    return new HandoverLocationRefDto(
        e.getId(),
        e.getKind(),
        e.getName(),
        e.getDescription(),
        e.getAddressLine(),
        e.getCountryCode(),
        e.getSurchargeEur() != null ? e.getSurchargeEur() : BigDecimal.ZERO);
  }
}
