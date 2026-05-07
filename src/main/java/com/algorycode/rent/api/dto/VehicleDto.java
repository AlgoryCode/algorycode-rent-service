package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** AlgoryRent FE {@code Vehicle} ile uyumlu alanlar. */
public record VehicleDto(
    Long id,
    Long vehicleModelId,
    Long vehicleStatusId,
    Long transmissionTypeId,
    Long bodyStyleId,
    Long fuelTypeId,
    String plate,
    String brand,
    String model,
    int year,
    VehicleStatus status,
    String statusCode,
    boolean external,
    String externalCompany,
    BigDecimal rentalDailyPrice,
    boolean commissionEnabled,
    BigDecimal commissionRatePercent,
    String commissionBrokerFullName,
    String commissionBrokerPhone,
    String countryCode,
    String engine,
    String fuelType,
    String bodyColor,
    Integer seats,
    Integer luggage,
    String transmissionType,
    String bodyStyleCode,
    String bodyStyleLabel,
    HandoverLocationRefDto defaultPickupHandoverLocation,
    /** İlk izin verilen teslim noktası (kirada varsayılan); liste boşsa null. */
    HandoverLocationRefDto defaultReturnHandoverLocation,
    /** İzin verilen tüm teslim (RETURN) noktaları, gösterim sırasıyla. */
    List<HandoverLocationRefDto> returnHandoverLocations,
    List<VehicleOptionDefinitionDto> optionDefinitions,
    /** Öne çıkan metinler (sıralı). */
    List<String> highlights,
    Map<String, String> images,
    /** user-fe {@code feFleetSnapshot} — vitrin JSON (DB + anlık üretim). */
    JsonNode feFleetSnapshot) {}
