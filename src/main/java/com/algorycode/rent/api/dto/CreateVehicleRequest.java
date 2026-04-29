package com.algorycode.rent.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record CreateVehicleRequest(
    @Size(max = 32) String plate,
    @NotNull Long vehicleModelId,
    Long vehicleStatusId,
    Integer year,
    Boolean external,
    @Size(max = 255) String externalCompany,
    BigDecimal rentalDailyPrice,
    BigDecimal commissionRatePercent,
    @Size(max = 32) String commissionBrokerPhone,
    @Size(max = 64) String countryCode,
    Long defaultPickupHandoverLocationId,
    Long defaultReturnHandoverLocationId,
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
    @Size(max = 30) List<@Size(max = 500) String> highlights) {}
