package com.algorycode.rent.api.mapper;

import com.algorycode.rent.api.dto.VehicleDto;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleImage;

import java.util.HashMap;
import java.util.Map;

public final class VehicleMapper {

  private VehicleMapper() {}

  public static VehicleDto toDto(Vehicle v) {
    Map<String, String> images = new HashMap<>();
    for (VehicleImage img : v.getImages()) {
      images.put(img.getSlot().name(), img.getImageUrl());
    }
    return new VehicleDto(
        v.getId(),
        v.getPlate(),
        v.getBrand(),
        v.getModel(),
        v.getYear(),
        v.isMaintenance(),
        v.getCountryCode(),
        Map.copyOf(images));
  }
}
