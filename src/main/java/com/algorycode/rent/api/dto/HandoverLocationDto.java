package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.location.HandoverLocationKind;

import java.math.BigDecimal;
import java.util.UUID;

public record HandoverLocationDto(
    UUID id,
    HandoverLocationKind kind,
    String name,
    String description,
    String addressLine,
    UUID cityId,
    String cityName,
    String countryCode,
    boolean active,
    int lineOrder,
    BigDecimal surchargeEur) {}
