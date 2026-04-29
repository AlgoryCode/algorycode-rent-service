package com.algorycode.rent.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record UpdateVehicleRequest(
    @Size(max = 32) String plate,
    Long vehicleModelId,
    Long vehicleStatusId,
    Integer year,
    Boolean external,
    @Size(max = 255) String externalCompany,
    BigDecimal rentalDailyPrice,
    BigDecimal commissionRatePercent,
    @Size(max = 32) String commissionBrokerPhone,
    @Size(max = 64) String countryCode,
    Long defaultPickupHandoverLocationId,
    /** Geriye uyumluluk; {@code returnHandoverLocationIds} null değilse yok sayılır. */
    Long defaultReturnHandoverLocationId,
    /**
     * null: teslim noktalarına dokunma. Boş liste: tüm araç-teslim eşlemelerini kaldır. Dolu: tamamen değiştir.
     */
    @Size(max = 50) List<Long> returnHandoverLocationIds,
    @Size(max = 100) List<Long> optionTemplateIds,
    @Size(max = 100) List<@Valid VehicleOptionDefinitionRequest> optionDefinitions,
    Map<String, String> images,
    @Size(max = 255) String engine,
    @Size(max = 64) String fuelType,
    @Size(max = 64) String bodyColor,
    Integer seats,
    Integer luggage,
    @Size(max = 32) String transmissionType,
    @Size(max = 32) String bodyStyleCode,
    /**
     * {@code null}: mevcut öne çıkanlara dokunma. Boş liste: tümünü sil. Dolu liste: tamamen
     * değiştir.
     */
    @Size(max = 30) List<@Size(max = 500) String> highlights) {}
