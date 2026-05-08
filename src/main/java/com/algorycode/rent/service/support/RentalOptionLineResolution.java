package com.algorycode.rent.service.support;

import com.algorycode.rent.dto.RentalOptionRequest;
import com.algorycode.rent.exception.BadRequestException;
import com.algorycode.rent.entity.ReservationExtraOptionTemplate;
import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.entity.VehicleOptionDefinition;
import com.algorycode.rent.repository.ReservationExtraOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;
import java.math.BigDecimal;

public final class RentalOptionLineResolution {

  public record Resolved(String title, String description, BigDecimal price, String icon) {}

  private RentalOptionLineResolution() {}

  public static Resolved resolve(
      Vehicle vehicle,
      RentalOptionRequest o,
      VehicleOptionDefinitionRepository definitionRepository,
      ReservationExtraOptionTemplateRepository reservationExtraTemplateRepository) {
    boolean hasVehicleDef = o.vehicleOptionDefinitionId() != null;
    boolean hasReservationTemplate = o.reservationExtraTemplateId() != null;
    if (hasVehicleDef && hasReservationTemplate) {
      throw new BadRequestException(
          "Aynı satırda hem araç seçeneği hem rezervasyon şablonu verilemez.");
    }
    if (hasVehicleDef) {
      if (vehicle == null) {
        throw new BadRequestException("Araç seçilmeden araç seçeneği gönderilemez.");
      }
      VehicleOptionDefinition def =
          definitionRepository
              .findByIdAndVehicle_Id(o.vehicleOptionDefinitionId(), vehicle.getId())
              .orElseThrow(() -> new BadRequestException("Geçersiz araç seçeneği."));
      if (!def.isActive()) {
        throw new BadRequestException("Seçilen araç seçeneği artık kullanılamaz.");
      }
      return new Resolved(
          def.getTitle(),
          def.getDescription() != null && !def.getDescription().isBlank()
              ? def.getDescription().trim()
              : null,
          def.getPrice(),
          def.getIcon() != null && !def.getIcon().isBlank() ? def.getIcon().trim() : null);
    }
    if (hasReservationTemplate) {
      ReservationExtraOptionTemplate t =
          reservationExtraTemplateRepository
              .findById(o.reservationExtraTemplateId())
              .orElseThrow(() -> new BadRequestException("Geçersiz rezervasyon ek seçeneği."));
      if (!t.isActive()) {
        throw new BadRequestException("Seçilen rezervasyon ek seçeneği artık kullanılamaz.");
      }
      return new Resolved(
          t.getTitle(),
          t.getDescription() != null && !t.getDescription().isBlank()
              ? t.getDescription().trim()
              : null,
          t.getPrice(),
          t.getIcon() != null && !t.getIcon().isBlank() ? t.getIcon().trim() : null);
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
