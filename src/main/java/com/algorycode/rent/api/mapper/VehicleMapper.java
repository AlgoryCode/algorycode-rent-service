package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.HandoverLocationRefDto;
import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.api.dto.VehicleOptionDefinitionDto;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleAllowedReturnHandover;
import com.algorycode.rent.domain.vehicle.VehicleHighlight;
import com.algorycode.rent.domain.vehicle.VehicleImage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class VehicleMapper {

  private VehicleMapper() {}

  public static VehicleDto toDto(Vehicle v) {
    Map<String, String> images = new HashMap<>();
    for (VehicleImage img : v.getImages()) {
      images.put(img.getSlot().name(), img.getImageUrl());
    }

    List<HandoverLocationRefDto> returnRefs =
        v.getAllowedReturnHandovers().stream()
            .sorted(
                Comparator.comparingInt(VehicleAllowedReturnHandover::getLineOrder)
                    .thenComparing(
                        VehicleAllowedReturnHandover::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
            .map(l -> HandoverLocationMapper.toRef(l.getHandoverLocation()))
            .toList();
    HandoverLocationRefDto firstReturn = returnRefs.isEmpty() ? null : returnRefs.get(0);

    String statusCode =
        v.getStatusDefinition().getCode() == null || v.getStatusDefinition().getCode().isBlank()
            ? v.getStatus().name()
            : v.getStatusDefinition().getCode().trim().toLowerCase(Locale.ROOT);
    return new VehicleDto(
        v.getId(),
        v.getVehicleModel().getId(),
        v.getStatusDefinition().getId(),
        v.getPlate(),
        v.getBrand(),
        v.getModel(),
        v.getYear() != null ? v.getYear() : 0,
        v.getStatus(),
        statusCode,
        v.isExternal(),
        v.getExternalCompany(),
        v.getRentalDailyPrice(),
        v.isCommissionEnabled(),
        v.getCommissionRatePercent(),
        v.getCommissionBrokerFullName(),
        v.getCommissionBrokerPhone(),
        v.getCountryCode(),
        v.getEngine(),
        v.getFuelType(),
        v.getBodyColor(),
        v.getSeats(),
        v.getLuggage(),
        v.getTransmissionType(),
        v.getBodyStyleCode(),
        null,
        HandoverLocationMapper.toRef(v.getDefaultPickupHandoverLocation()),
        firstReturn,
        returnRefs,
        v.getOptionDefinitions().stream()
            .map(
                d ->
                    new VehicleOptionDefinitionDto(
                        d.getId(),
                        d.getTitle(),
                        d.getDescription(),
                        d.getPrice(),
                        d.getIcon(),
                        d.getLineOrder(),
                        d.isActive()))
            .toList(),
        v.getHighlights().stream()
            .sorted((a, b) -> Integer.compare(a.getLineOrder(), b.getLineOrder()))
            .map(VehicleHighlight::getText)
            .toList(),
        Map.copyOf(images),
        null);
  }
}
