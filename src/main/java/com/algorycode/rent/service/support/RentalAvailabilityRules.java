package com.algorycode.rent.service.support;

import java.time.LocalDate;

/**
 * Kiralama takvimi: kapsayıcı tarih çakışması + bitişten sonraki günün boş kalması (rent a car
 * tampon günü). İptaller hariç tutulur çağıran tarafta.
 */
public final class RentalAvailabilityRules {

  private RentalAvailabilityRules() {}

  /** İki kapalı aralık kesişiyor mu (uçlar dahil). */
  public static boolean dateRangesOverlap(
      LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
    return !aStart.isAfter(bEnd) && !bStart.isAfter(aEnd);
  }

  /**
   * Mevcut bir kiralama kaydı, talep edilen [reqStart, reqEnd] aralığını engelliyor mu? - Doğrudan
   * tarih çakışması - Talep bitişinin ertesi günü (reqEnd+1) başka bir kiralama tarafından işgal
   * ediliyorsa (teslim sonrası tampon gün)
   */
  public static boolean rentalBlocksRequestedRange(
      LocalDate reqStart, LocalDate reqEnd, LocalDate rentalStart, LocalDate rentalEnd) {
    if (dateRangesOverlap(reqStart, reqEnd, rentalStart, rentalEnd)) {
      return true;
    }
    if (reqEnd.equals(LocalDate.MAX)) {
      return false;
    }
    LocalDate dayAfterRequest = reqEnd.plusDays(1);
    return dateRangesOverlap(rentalStart, rentalEnd, dayAfterRequest, dayAfterRequest);
  }
}
