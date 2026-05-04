package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.location.HandoverLocationKind;
import java.math.BigDecimal;

/** Kiralama / talep yanıtında seçilen alış veya teslim noktası özeti. */
public record HandoverLocationRefDto(
    Long id,
    HandoverLocationKind kind,
    String name,
    String description,
    String addressLine,
    String countryCode,
    BigDecimal surchargeEur) {}
