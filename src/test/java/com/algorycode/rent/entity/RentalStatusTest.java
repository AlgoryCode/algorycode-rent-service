package com.algorycode.rent.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RentalStatusTest {

  @Test
  void tryParse_acceptsTurkishLabels() {
    assertThat(RentalStatus.tryParse("İptal")).contains(RentalStatus.CANCELLED);
    assertThat(RentalStatus.tryParse("Onay Bekliyor")).contains(RentalStatus.PENDING);
    assertThat(RentalStatus.tryParse("aktif")).contains(RentalStatus.ACTIVE);
  }
}
