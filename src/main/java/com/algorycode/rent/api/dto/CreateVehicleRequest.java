package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateVehicleRequest(
    @NotBlank @Size(max = 32) String plate,
    @NotBlank @Size(max = 255) String brand,
    @NotBlank @Size(max = 255) String model,
    @NotNull @Min(1950) @Max(2100) Integer year,
    boolean maintenance,
    /** Araç başka firmadan geldiyse işaretleyin. */
    boolean external,
    @Size(max = 255) String externalCompany,
    @NotNull @DecimalMin(value = "0.01", inclusive = true)
    BigDecimal rentalDailyPrice,
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal commissionRatePercent,
    @Size(max = 32) String commissionBrokerPhone,
    @NotNull UUID cityId,
    /** Kiralama başlangıcında kullanılacak varsayılan alış noktası (PICKUP türü). */
    @NotNull UUID defaultPickupHandoverLocationId,
    UUID defaultReturnHandoverLocationId,
    @Size(max = 100) List<@Valid VehicleOptionDefinitionRequest> optionDefinitions,
    Map<String, String> images,
    @Size(max = 255) String engine,
    @Min(1) @Max(20) Integer seats,
    @Min(0) Integer luggage) {}
