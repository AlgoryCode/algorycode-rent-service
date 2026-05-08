package com.algorycode.rent.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RentalStatusTest {

  @Test
  void tryParse_acceptsTurkishLabels() {
    assertThat(RentalStatus.tryParse("İptal")).contains(RentalStatus.cancelled);
    assertThat(RentalStatus.tryParse("Onay Bekliyor")).contains(RentalStatus.pending);
    assertThat(RentalStatus.tryParse("aktif")).contains(RentalStatus.active);
  }
}
