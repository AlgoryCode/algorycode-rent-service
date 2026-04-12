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
    BigDecimal rentalDailyPrice,
    boolean commissionEnabled,
    BigDecimal commissionRatePercent,
    String commissionBrokerFullName,
    String commissionBrokerPhone,
    String countryCode,
    String countryName,
    UUID cityId,
    String cityName,
    String engine,
    Integer seats,
    Integer luggage,
    Map<String, String> images) {}
