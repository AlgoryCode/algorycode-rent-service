package com.algorycode.rent.service.support;

import com.algorycode.rent.exception.BadRequestException;
import java.time.LocalDate;

/** Kiralama / liste filtreleri için başlangıç–bitiş tarih sırası doğrulaması. */
public final class DateRangeValidator {

  public static final String MSG_END_BEFORE_START = "Bitiş tarihi başlangıçtan önce olamaz.";

  private DateRangeValidator() {}

  /** {@code end} &lt; {@code start} ise {@link BadRequestException}. */
  public static void requireEndNotBeforeStart(LocalDate start, LocalDate end) {
    if (end.isBefore(start)) {
      throw new BadRequestException(MSG_END_BEFORE_START);
    }
  }

  /**
   * İkisi de doluysa sıra kontrolü; biri {@code null} ise işlem yok (liste filtreleri gibi
   * opsiyonel aralıklar).
   */
  public static void requireEndNotBeforeStartIfBothPresent(LocalDate start, LocalDate end) {
    if (start != null && end != null) {
      requireEndNotBeforeStart(start, end);
    }
  }
}
