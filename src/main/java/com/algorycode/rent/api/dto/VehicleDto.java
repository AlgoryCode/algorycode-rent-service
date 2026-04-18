package com.algorycode.rent.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    HandoverLocationRefDto defaultPickupHandoverLocation,
    /** İlk izin verilen teslim noktası (kirada varsayılan); liste boşsa null. */
    HandoverLocationRefDto defaultReturnHandoverLocation,
    /** İzin verilen tüm teslim (RETURN) noktaları, gösterim sırasıyla. */
    List<HandoverLocationRefDto> returnHandoverLocations,
    List<VehicleOptionDefinitionDto> optionDefinitions,
    /** Öne çıkan metinler (sıralı). */
    List<String> highlights,
    Map<String, String> images) {}
