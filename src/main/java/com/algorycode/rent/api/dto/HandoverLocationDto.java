package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.location.HandoverLocationKind;

import java.math.BigDecimal;

public record HandoverLocationDto(
    Long id,
    HandoverLocationKind kind,
    String name,
    String description,
    String addressLine,
    Long cityId,
    String cityName,
    String countryCode,
    boolean active,
    int lineOrder,
    BigDecimal surchargeEur) {}
