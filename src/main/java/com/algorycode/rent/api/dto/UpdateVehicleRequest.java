package com.algorycode.rent.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    UUID cityId,
    UUID defaultPickupHandoverLocationId,
    /** Geriye uyumluluk; {@code returnHandoverLocationIds} null değilse yok sayılır. */
    UUID defaultReturnHandoverLocationId,
    /**
     * null: teslim noktalarına dokunma. Boş liste: tüm araç-teslim eşlemelerini kaldır. Dolu: tamamen değiştir.
     */
    @Size(max = 50) List<UUID> returnHandoverLocationIds,
    @Size(max = 100) List<UUID> optionTemplateIds,
    @Size(max = 100) List<@Valid VehicleOptionDefinitionRequest> optionDefinitions,
    Map<String, String> images,
    @Size(max = 255) String engine,
    @Size(max = 64) String fuelType,
    @Size(max = 64) String bodyColor,
    @Min(1) @Max(20) Integer seats,
    @Min(0) Integer luggage,
    @Size(max = 32) String transmissionType,
    @Size(max = 32) String bodyStyleCode,
    /**
     * {@code null}: mevcut öne çıkanlara dokunma. Boş liste: tümünü sil. Dolu liste: tamamen
     * değiştir.
     */
    @Size(max = 30) List<@NotBlank @Size(max = 500) String> highlights) {}
