package com.algorycode.rent.dto;

import com.algorycode.rent.entity.HandoverLocationKind;
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
