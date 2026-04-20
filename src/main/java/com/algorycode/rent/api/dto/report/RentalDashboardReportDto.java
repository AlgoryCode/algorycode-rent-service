package com.algorycode.rent.api.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Kiralama geliri: ozet, arac bazli ve zamansal (gun veya ay). */
public record RentalDashboardReportDto(
    LocalDate fromInclusive,
    LocalDate toInclusive,
    /** {@code day} veya {@code month} — aralik uzunluguna gore. */
    String timelineGranularity,
    RentalReportSummary summary,
    List<VehicleRentalStatRow> byVehicle,
    List<TimelineBucket> timeline) {

  public record RentalReportSummary(
      int rentalCount,
      long rentalDayBooked,
      BigDecimal totalRevenueEur,
      BigDecimal totalBaseRentalEur,
      BigDecimal totalOptionsEur,
      BigDecimal totalCommissionEur,
      int activeOrPendingCount,
      int completedCount) {}

  public record VehicleRentalStatRow(
      Long vehicleId,
      String plate,
      String brand,
      String model,
      int rentalCount,
      long rentalDayBooked,
      BigDecimal revenueEur,
      BigDecimal baseRentalEur,
      BigDecimal optionsEur) {}

  /** Gelir, kiralama baslangic tarihine (veya aya) yazilir. */
  public record TimelineBucket(String period, String label, int rentalStarts, BigDecimal revenueEur) {}
}
