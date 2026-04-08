package com.algorycode.rent.api.dto;

import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

/** AlgoryRent FE {@code Vehicle} ile uyumlu alanlar. */
public record VehicleDto(
    UUID id,
    String plate,
    String brand,
    String model,
    int year,
    boolean maintenance,
    boolean external,
    String externalCompany,
    BigDecimal defaultCommissionAmount,
    String countryCode,
    Map<String, String> images) {}
