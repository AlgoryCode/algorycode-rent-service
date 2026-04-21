package com.algorycode.rent.service.support;

import java.time.LocalDate;

/**
 * Kapalı (uçlar dahil) tarih aralığı; uygunluk takvimi birleştirme ve boş pencere hesapları için.
 */
public record InclusiveLocalDateRange(LocalDate start, LocalDate end) {

  public InclusiveLocalDateRange {
    if (start == null || end == null) {
      throw new IllegalArgumentException("start and end are required");
    }
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("start must not be after end");
    }
  }
}
