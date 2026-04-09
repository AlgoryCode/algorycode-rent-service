package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public record UpdateVehicleRequest(
    @Size(max = 32) String plate,
    @Size(max = 255) String brand,
    @Size(max = 255) String model,
    @Min(1950) @Max(2100) Integer year,
    Boolean maintenance,
    Boolean external,
    @Size(max = 255) String externalCompany,
    @DecimalMin(value = "0.01", inclusive = true) BigDecimal rentalDailyPrice,
    @DecimalMin(value = "0.0", inclusive = true) BigDecimal commissionRatePercent,
    @Size(max = 32) String commissionBrokerPhone,
    /** ISO 3166-1 alpha-2 veya boş */
    @Size(min = 2, max = 2) String countryCode,
    Map<String, String> images) {}
