package com.algorycode.rent.service.support;

import com.algorycode.rent.api.dto.RentalOptionRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleOptionDefinition;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;

import java.math.BigDecimal;

public final class RentalOptionLineResolution {

  public record Resolved(String title, String description, BigDecimal price, String icon) {}

  private RentalOptionLineResolution() {}

  public static Resolved resolve(
      Vehicle vehicle, RentalOptionRequest o, VehicleOptionDefinitionRepository definitionRepository) {
    if (o.vehicleOptionDefinitionId() != null) {
      VehicleOptionDefinition def =
          definitionRepository
              .findByIdAndVehicle_Id(o.vehicleOptionDefinitionId(), vehicle.getId())
              .orElseThrow(() -> new BadRequestException("Geçersiz araç seçeneği."));
      if (!def.isActive()) {
        throw new BadRequestException("Seçilen araç seçeneği artık kullanılamaz.");
      }
      return new Resolved(
          def.getTitle(),
          def.getDescription() != null && !def.getDescription().isBlank() ? def.getDescription().trim() : null,
          def.getPrice(),
          def.getIcon() != null && !def.getIcon().isBlank() ? def.getIcon().trim() : null);
    }
    if (o.title() == null || o.title().isBlank()) {
      throw new BadRequestException("Seçenek başlığı zorunludur.");
    }
    if (o.price() == null) {
      throw new BadRequestException("Seçenek fiyatı zorunludur.");
    }
    return new Resolved(
        o.title().trim(),
        o.description() != null && !o.description().isBlank() ? o.description().trim() : null,
        o.price(),
        o.icon() != null && !o.icon().isBlank() ? o.icon().trim() : null);
  }
}
