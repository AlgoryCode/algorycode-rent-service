package com.algorycode.rent.service.support;

import com.algorycode.rent.api.dto.CreateRentalRequestFormRequest;
import com.algorycode.rent.api.dto.HandoverPricingQuoteDto;
import com.algorycode.rent.api.dto.RentalOptionRequest;
import com.algorycode.rent.config.AppRentalRequestProperties;
import com.algorycode.rent.domain.request.RentalRequest;
import com.algorycode.rent.domain.request.RentalRequestPricedLine;
import com.algorycode.rent.domain.request.RentalRequestPricedLineType;
import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.repository.ReservationExtraOptionTemplateRepository;
import com.algorycode.rent.repository.VehicleOptionDefinitionRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Talep oluşturulurken faturalandırma satırlarını sunucu tarafında üretir (TRY; handover EUR metadata). */
@Component
public class RentalRequestPricedLineAssembler {

  private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

  private final VehicleOptionDefinitionRepository vehicleOptionDefinitionRepository;
  private final ReservationExtraOptionTemplateRepository reservationExtraOptionTemplateRepository;
  private final AppRentalRequestProperties rentalRequestProperties;

  public RentalRequestPricedLineAssembler(
      VehicleOptionDefinitionRepository vehicleOptionDefinitionRepository,
      ReservationExtraOptionTemplateRepository reservationExtraOptionTemplateRepository,
      AppRentalRequestProperties rentalRequestProperties) {
    this.vehicleOptionDefinitionRepository = vehicleOptionDefinitionRepository;
    this.reservationExtraOptionTemplateRepository = reservationExtraOptionTemplateRepository;
    this.rentalRequestProperties = rentalRequestProperties;
  }

  /** FE {@code rentalNights} ile uyumlu: tam gün farkı, en az 1. */
  public static int rentalNightsBetween(LocalDate start, LocalDate end) {
    if (start == null || end == null) {
      return 0;
    }
    long d = ChronoUnit.DAYS.between(start, end);
    return (int) Math.max(1, d);
  }

  public void attach(
      RentalRequest entity,
      Vehicle vehicle,
      CreateRentalRequestFormRequest req,
      HandoverPricingQuoteDto handoverQuote,
      int rentalNights,
      BigDecimal greenInsuranceFee) {
    entity.getPricedLines().clear();
    BigDecimal tryPerEur = rentalRequestProperties.tryPerEur().setScale(4, RoundingMode.HALF_UP);
    int order = 0;
    Instant pricedAt = Instant.now();

    if (vehicle != null
        && vehicle.getRentalDailyPrice() != null
        && rentalNights > 0
        && vehicle.getRentalDailyPrice().compareTo(ZERO) > 0) {
      BigDecimal unit = vehicle.getRentalDailyPrice().setScale(2, RoundingMode.HALF_UP);
      BigDecimal lineAmt = unit.multiply(BigDecimal.valueOf(rentalNights)).setScale(2, RoundingMode.HALF_UP);
      RentalRequestPricedLine row = line(entity, RentalRequestPricedLineType.BASE_RENTAL, "Günlük kiralama", null, order++);
      row.setQuantity(rentalNights);
      row.setUnitAmount(unit);
      row.setLineAmount(lineAmt);
      row.setCurrency("TRY");
      row.setPricedAt(pricedAt);
      row.setMetadata(
          "{\"nights\":"
              + rentalNights
              + ",\"dailyTry\":\""
              + unit.toPlainString()
              + "\"}");
      entity.getPricedLines().add(row);
    }

    if (handoverQuote != null && handoverQuote.totalEur().compareTo(ZERO) > 0) {
      BigDecimal totalEur = handoverQuote.totalEur().setScale(2, RoundingMode.HALF_UP);
      BigDecimal lineTry =
          totalEur.multiply(tryPerEur).setScale(2, RoundingMode.HALF_UP);
      RentalRequestPricedLine row = line(entity, RentalRequestPricedLineType.HANDOVER_SURCHARGE, "Teslim / nokta ek ücreti", null, order++);
      row.setQuantity(1);
      row.setUnitAmount(lineTry);
      row.setLineAmount(lineTry);
      row.setCurrency("TRY");
      row.setPricedAt(pricedAt);
      if (entity.getReturnHandoverLocation() != null) {
        row.setReturnHandoverLocationId(entity.getReturnHandoverLocation().getId());
      }
      row.setMetadata(
          "{\"pickupLegEur\":\""
              + handoverQuote.pickupLegEur().toPlainString()
              + "\",\"returnLegEur\":\""
              + handoverQuote.returnLegEur().toPlainString()
              + "\",\"routeEur\":\""
              + handoverQuote.routeEur().toPlainString()
              + "\",\"totalEur\":\""
              + totalEur.toPlainString()
              + "\",\"fxTryPerEur\":\""
              + tryPerEur.toPlainString()
              + "\"}");
      entity.getPricedLines().add(row);
    }

    if (greenInsuranceFee != null && greenInsuranceFee.compareTo(ZERO) > 0) {
      BigDecimal g = greenInsuranceFee.setScale(2, RoundingMode.HALF_UP);
      RentalRequestPricedLine row = line(entity, RentalRequestPricedLineType.ABROAD_USAGE, "Yurt dışı kullanım (yeşil sigorta)", null, order++);
      row.setQuantity(1);
      row.setUnitAmount(g);
      row.setLineAmount(g);
      row.setCurrency("TRY");
      row.setPricedAt(pricedAt);
      row.setMetadata("{\"outsideCountryTravel\":true}");
      entity.getPricedLines().add(row);
    }

    if (req.options() != null) {
      for (RentalOptionRequest o : req.options()) {
        RentalOptionLineResolution.Resolved resolved =
            RentalOptionLineResolution.resolve(
                vehicle, o, vehicleOptionDefinitionRepository, reservationExtraOptionTemplateRepository);
        RentalRequestPricedLineType type;
        Long srcVeh = null;
        Long srcTpl = null;
        if (o.vehicleOptionDefinitionId() != null) {
          type = RentalRequestPricedLineType.VEHICLE_OPTION;
          srcVeh = o.vehicleOptionDefinitionId();
        } else if (o.reservationExtraTemplateId() != null) {
          type = RentalRequestPricedLineType.RESERVATION_EXTRA;
          srcTpl = o.reservationExtraTemplateId();
        } else {
          type = RentalRequestPricedLineType.CUSTOM_LINE;
        }
        BigDecimal price = resolved.price().setScale(2, RoundingMode.HALF_UP);
        RentalRequestPricedLine row = line(entity, type, resolved.title(), resolved.description(), order++);
        row.setQuantity(1);
        row.setUnitAmount(price);
        row.setLineAmount(price);
        row.setCurrency("TRY");
        row.setPricedAt(pricedAt);
        row.setSourceVehicleOptionDefinitionId(srcVeh);
        row.setSourceReservationExtraTemplateId(srcTpl);
        if (srcTpl != null) {
          reservationExtraOptionTemplateRepository
              .findById(srcTpl)
              .ifPresent(
                  t ->
                      row.setMetadata(
                          "{\"templateCode\":\""
                              + escapeJson(t.getCode())
                              + "\",\"templateUpdatedAt\":\""
                              + t.getUpdatedAt().toString()
                              + "\"}"));
        }
        entity.getPricedLines().add(row);
      }
    }

    BigDecimal sum =
        entity.getPricedLines().stream()
            .map(RentalRequestPricedLine::getLineAmount)
            .reduce(ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    entity.setPricingTotalTry(sum);
  }

  private static String escapeJson(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static RentalRequestPricedLine line(
      RentalRequest entity, RentalRequestPricedLineType type, String title, String description, int lineOrder) {
    RentalRequestPricedLine row = new RentalRequestPricedLine();
    row.setRentalRequest(entity);
    row.setLineType(type);
    row.setTitle(title);
    row.setDescription(description);
    row.setLineOrder(lineOrder);
    return row;
  }
}
