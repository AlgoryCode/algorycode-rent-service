package com.algorycode.rent.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class VehicleTransmissionKindTest {

  @Test
  void tryParse_whenTurkishManual_thenManual() {
    assertThat(VehicleTransmissionKind.tryParse("Manuel")).contains(VehicleTransmissionKind.manual);
  }

  @Test
  void tryParse_whenCode_thenMatch() {
    assertThat(VehicleTransmissionKind.tryParse("automatic")).contains(VehicleTransmissionKind.automatic);
  }

  @Test
  void stableId_matchesCatalogSeedOrder() {
    assertThat(VehicleTransmissionKind.manual.getStableId()).isEqualTo(1L);
    assertThat(VehicleTransmissionKind.automatic.getStableId()).isEqualTo(2L);
    assertThat(VehicleTransmissionKind.semi_automatic.getStableId()).isEqualTo(3L);
  }

  @Test
  void parseRequired_whenUnknown_thenThrows() {
    assertThatThrownBy(() -> VehicleTransmissionKind.parseRequired("xyz"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
