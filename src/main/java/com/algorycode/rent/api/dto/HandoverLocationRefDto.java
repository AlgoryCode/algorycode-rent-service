package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.location.HandoverLocationKind;

import java.math.BigDecimal;
import java.util.UUID;

/** Kiralama / talep yanıtında seçilen alış veya teslim noktası özeti. */
public record HandoverLocationRefDto(
    UUID id,
    HandoverLocationKind kind,
    String name,
    String description,
    String addressLine,
    UUID cityId,
    String cityName,
    String countryCode,
    BigDecimal surchargeEur) {}
