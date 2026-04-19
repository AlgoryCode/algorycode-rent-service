package com.algorycode.rent.service.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RentalAvailabilityRulesTest {

  @Test
  void singleDayBeforeBlockedNextDay_excludes() {
    // Kiralama 15-16; talep 14-14 → 15 dolu (bitiş+1 tamponu) engeller
    assertThat(
            RentalAvailabilityRules.rentalBlocksRequestedRange(
                LocalDate.of(2026, 6, 14),
                LocalDate.of(2026, 6, 14),
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16)))
        .isTrue();
  }

  @Test
  void singleDayWhenDayAfterOccupied_excludes() {
    // Talep 17-17; 18 dolu → 18 talep bitişinin ertesi günü
    assertThat(
            RentalAvailabilityRules.rentalBlocksRequestedRange(
                LocalDate.of(2026, 6, 17),
                LocalDate.of(2026, 6, 17),
                LocalDate.of(2026, 6, 18),
                LocalDate.of(2026, 6, 20)))
        .isTrue();
  }

  @Test
  void gapDayBetweenRentals_allows() {
    assertThat(
            RentalAvailabilityRules.rentalBlocksRequestedRange(
                LocalDate.of(2026, 6, 17),
                LocalDate.of(2026, 6, 17),
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16)))
        .isFalse();
  }

  @Test
  void directOverlap_blocks() {
    assertThat(
            RentalAvailabilityRules.rentalBlocksRequestedRange(
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16)))
        .isTrue();
  }
}
