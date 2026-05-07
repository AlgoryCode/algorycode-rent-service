package com.algorycode.rent.service.support;

import com.algorycode.rent.domain.rental.Rental;
import com.algorycode.rent.domain.request.RentalRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Araç uygunluğu: iptal olmayan kiralama ve engelleyici taleplerin tarih aralıkları birleştirilir;
 * kullanıcının kapalı [tripStart, tripEnd] aralığı bu birleşik dolulukla kesişmeden seçilebiliyorsa
 * iç takvim uygundur. Buna ek olarak {@link RentalAvailabilityRules} ile uyumlu olarak, talep
 * bitişinin ertesi günü başka bir kiralama/talep tarafından işgal edilmemelidir (tampon günü).
 */
@Component
public class VehicleAvailabilitySlotAnalyzer {

  /** Bitişi ertesi gün ile bitişi kıyaslayan birleştirme (bitişik aralıklar tek blok olur). */
  private static final int MERGE_IF_START_ON_OR_BEFORE_END_PLUS_DAYS = 1;

  /**
   * Verilen araç için [tripStart, tripEnd] (dahil) kiralama penceresi listelemeye uygun mu?
   *
   * @param vehicleId araç kimliği
   * @param tripStart kiralama başlangıcı (dahil)
   * @param tripEnd kiralama bitişi (dahil)
   * @param rentalCandidates önceden süzülmüş kiralama adayları
   * @param requestCandidates önceden süzülmüş talep adayları
   * @return iç çakışma yok ve turnover günü boşsa {@code true}
   */
  public boolean isAvailableForInclusiveTrip(
      Long vehicleId,
      LocalDate tripStart,
      LocalDate tripEnd,
      List<Rental> rentalCandidates,
      List<RentalRequest> requestCandidates) {
    List<InclusiveLocalDateRange> merged =
        mergeSorted(collectRawIntervals(vehicleId, rentalCandidates, requestCandidates));
    if (!inclusiveTripFitsInAFreeSlot(tripStart, tripEnd, tripStart, tripEnd, merged)) {
      return false;
    }
    return !turnoverDayBlocked(vehicleId, tripEnd, rentalCandidates, requestCandidates);
  }

  /**
   * [windowStart, windowEnd] içinde, birleştirilmiş dolulukların dışında kalan boş aralıklar (uçlar
   * dahil). Liste boş olabilir; tüm pencere doluysa boş liste döner.
   */
  public List<InclusiveLocalDateRange> freeSlotsInWindow(
      LocalDate windowStart,
      LocalDate windowEnd,
      List<InclusiveLocalDateRange> mergedBlockingSorted) {
    if (windowStart.isAfter(windowEnd)) {
      throw new IllegalArgumentException("windowStart must not be after windowEnd");
    }
    List<InclusiveLocalDateRange> free = new ArrayList<>();
    LocalDate cursor = windowStart;
    for (InclusiveLocalDateRange block : mergedBlockingSorted) {
      if (block.end().isBefore(windowStart)) {
        continue;
      }
      if (block.start().isAfter(windowEnd)) {
        break;
      }
      LocalDate blockStart = max(block.start(), windowStart);
      LocalDate blockEnd = min(block.end(), windowEnd);
      if (cursor.isBefore(blockStart)) {
        free.add(new InclusiveLocalDateRange(cursor, blockStart.minusDays(1)));
      }
      cursor = blockEnd.plusDays(1);
      if (cursor.isAfter(windowEnd)) {
        return free;
      }
    }
    if (!cursor.isAfter(windowEnd)) {
      free.add(new InclusiveLocalDateRange(cursor, windowEnd));
    }
    return free;
  }

  /** Araç için ham doluluk aralıklarını döndürür (henüz birleştirilmemiş). */
  public List<InclusiveLocalDateRange> collectRawIntervals(
      Long vehicleId, List<Rental> rentals, List<RentalRequest> requests) {
    return rawIntervals(vehicleId, rentals, requests);
  }

  /** Aralıkları sıralayıp bitişik/çakışan olanları birleştirir. */
  public List<InclusiveLocalDateRange> mergeSorted(List<InclusiveLocalDateRange> intervals) {
    if (intervals.isEmpty()) {
      return List.of();
    }
    List<InclusiveLocalDateRange> sorted =
        intervals.stream().sorted(Comparator.comparing(InclusiveLocalDateRange::start)).toList();
    List<InclusiveLocalDateRange> merged = new ArrayList<>();
    LocalDate curStart = sorted.getFirst().start();
    LocalDate curEnd = sorted.getFirst().end();
    for (int i = 1; i < sorted.size(); i++) {
      InclusiveLocalDateRange next = sorted.get(i);
      LocalDate mergeIfStartOnOrBefore = curEnd.plusDays(MERGE_IF_START_ON_OR_BEFORE_END_PLUS_DAYS);
      if (!next.start().isAfter(mergeIfStartOnOrBefore)) {
        if (next.end().isAfter(curEnd)) {
          curEnd = next.end();
        }
      } else {
        merged.add(new InclusiveLocalDateRange(curStart, curEnd));
        curStart = next.start();
        curEnd = next.end();
      }
    }
    merged.add(new InclusiveLocalDateRange(curStart, curEnd));
    return merged;
  }

  /** [tripStart, tripEnd] tamamen tek bir boş pencereye sığıyor mu (pencere içi complement ile). */
  public boolean inclusiveTripFitsInAFreeSlot(
      LocalDate tripStart,
      LocalDate tripEnd,
      LocalDate windowStart,
      LocalDate windowEnd,
      List<InclusiveLocalDateRange> mergedBlockingSorted) {
    List<InclusiveLocalDateRange> free =
        freeSlotsInWindow(windowStart, windowEnd, mergedBlockingSorted);
    for (InclusiveLocalDateRange slot : free) {
      if (!slot.start().isAfter(tripStart) && !slot.end().isBefore(tripEnd)) {
        return true;
      }
    }
    return false;
  }

  private static List<InclusiveLocalDateRange> rawIntervals(
      Long vehicleId, List<Rental> rentals, List<RentalRequest> requests) {
    List<InclusiveLocalDateRange> out = new ArrayList<>();
    for (Rental r : rentals) {
      Long vid = rentalVehicleId(r);
      if (vid == null || !vid.equals(vehicleId)) {
        continue;
      }
      out.add(new InclusiveLocalDateRange(r.getStartDate(), r.getEndDate()));
    }
    for (RentalRequest rr : requests) {
      Long vid = requestVehicleId(rr);
      if (vid == null || !vid.equals(vehicleId)) {
        continue;
      }
      out.add(new InclusiveLocalDateRange(rr.getStartDate(), rr.getEndDate()));
    }
    return out;
  }

  private static boolean turnoverDayBlocked(
      Long vehicleId,
      LocalDate tripEnd,
      List<Rental> rentalCandidates,
      List<RentalRequest> requestCandidates) {
    if (tripEnd.equals(LocalDate.MAX)) {
      return false;
    }
    LocalDate dayAfterTrip = tripEnd.plusDays(1);
    for (Rental r : rentalCandidates) {
      Long vid = rentalVehicleId(r);
      if (vid == null || !vid.equals(vehicleId)) {
        continue;
      }
      if (RentalAvailabilityRules.dateRangesOverlap(
          dayAfterTrip, dayAfterTrip, r.getStartDate(), r.getEndDate())) {
        return true;
      }
    }
    for (RentalRequest rr : requestCandidates) {
      Long vid = requestVehicleId(rr);
      if (vid == null || !vid.equals(vehicleId)) {
        continue;
      }
      if (RentalAvailabilityRules.dateRangesOverlap(
          dayAfterTrip, dayAfterTrip, rr.getStartDate(), rr.getEndDate())) {
        return true;
      }
    }
    return false;
  }

  private static Long rentalVehicleId(Rental r) {
    if (r.getVehicleId() != null) {
      return r.getVehicleId();
    }
    return r.getVehicle() != null ? r.getVehicle().getId() : null;
  }

  private static Long requestVehicleId(RentalRequest rr) {
    if (rr.getVehicleId() != null) {
      return rr.getVehicleId();
    }
    return rr.getVehicle() != null ? rr.getVehicle().getId() : null;
  }

  private static LocalDate max(LocalDate a, LocalDate b) {
    return a.isAfter(b) ? a : b;
  }

  private static LocalDate min(LocalDate a, LocalDate b) {
    return a.isBefore(b) ? a : b;
  }
}
