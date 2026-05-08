package com.algorycode.rent.service.support;

import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalCommissionFlow;
import com.algorycode.rent.entity.Vehicle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Kiralama satırına komisyon tutarı saklamıyoruz; günlük kiralama (gün × günlük fiyat) üzerinden
 * araçtan türetilen komisyon yalnızca net hesap ve JSON DTO görünümü için kullanılır.
 */
public final class RentalCommissionFromVehicle {

  private RentalCommissionFromVehicle() {}

  public record Snapshot(BigDecimal amount, RentalCommissionFlow flow, String company) {}

  public static BigDecimal baseRentalCharge(
      LocalDate start, LocalDate end, BigDecimal rentalDailyPrice) {
    long days = ChronoUnit.DAYS.between(start, end) + 1;
    BigDecimal daily = rentalDailyPrice != null ? rentalDailyPrice : BigDecimal.ZERO;
    return daily.multiply(BigDecimal.valueOf(days));
  }

  public static BigDecimal baseRentalCharge(Rental rental, Vehicle vehicle) {
    return baseRentalCharge(
        rental.getStartDate(), rental.getEndDate(), vehicle.getRentalDailyPrice());
  }

  public static Snapshot deriveSnapshot(Vehicle vehicle, BigDecimal baseRentalCharge) {
    if (vehicle == null
        || !vehicle.isCommissionEnabled()
        || vehicle.getCommissionRatePercent() == null
        || baseRentalCharge == null) {
      return clearedSnapshot();
    }
    BigDecimal raw =
        baseRentalCharge
            .multiply(vehicle.getCommissionRatePercent())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    if (raw.compareTo(BigDecimal.ZERO) <= 0) {
      return clearedSnapshot();
    }
    RentalCommissionFlow flow =
        vehicle.isExternal() ? RentalCommissionFlow.pay : RentalCommissionFlow.collect;
    String company = vehicle.isExternal() ? Text.cleanOrNull(vehicle.getExternalCompany()) : null;
    return new Snapshot(raw, flow, company);
  }

  public static Snapshot deriveSnapshot(Rental rental, Vehicle vehicle) {
    return deriveSnapshot(vehicle, baseRentalCharge(rental, vehicle));
  }

  /** Oluşturma öncesi: harici + pozitif komisyonda araç firma bilgisi zorunlu. */
  public static void validateDerivedOrThrow(Snapshot snapshot) {
    RentalCommissionValidator.validate(snapshot.amount(), snapshot.flow(), snapshot.company());
  }

  public static Snapshot clearedSnapshot() {
    return new Snapshot(
        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), RentalCommissionFlow.collect, null);
  }
}
