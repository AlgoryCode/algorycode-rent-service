package com.algorycode.rent.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** AlgoryRent FE {@code Vehicle} ile uyumlu alanlar. */
public record VehicleDto(
    Long id,
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
    Long cityId,
    String cityName,
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
