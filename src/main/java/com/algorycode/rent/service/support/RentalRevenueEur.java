package com.algorycode.rent.service.support;

import com.algorycode.rent.entity.Rental;
import com.algorycode.rent.entity.RentalOption;
import com.algorycode.rent.entity.Vehicle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Rapor ve ödeme kayıtlarında kullanılan: günlük fiyat × gün + opsiyonlar (EUR). */
public final class RentalRevenueEur {

  private RentalRevenueEur() {}

  public static long inclusiveRentalDays(LocalDate start, LocalDate end) {
    if (start == null || end == null || end.isBefore(start)) {
      return 0;
    }
    return ChronoUnit.DAYS.between(start, end) + 1;
  }

  public static BigDecimal baseRentalEur(Rental r) {
    Vehicle v = r.getVehicle();
    BigDecimal daily =
        v != null && v.getRentalDailyPrice() != null ? v.getRentalDailyPrice() : BigDecimal.ZERO;
    long days = inclusiveRentalDays(r.getStartDate(), r.getEndDate());
    return daily.multiply(BigDecimal.valueOf(days));
  }

  public static BigDecimal optionsTotal(Rental r) {
    BigDecimal s = BigDecimal.ZERO;
    if (r.getOptions() != null) {
      for (RentalOption o : r.getOptions()) {
        if (o.getPrice() != null) {
          s = s.add(o.getPrice());
        }
      }
    }
    return s;
  }

  public static BigDecimal totalRentalRevenueEur(Rental r) {
    return baseRentalEur(r).add(optionsTotal(r)).setScale(2, RoundingMode.HALF_UP);
  }
}
