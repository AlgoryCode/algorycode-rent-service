package com.algorycode.rent.domain.vehicle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VehicleStatusTest {

  @Test
  void fromCode_whenUnknownCode_thenReturnsAvailable() {
    assertThat(VehicleStatus.fromCode("custom_status")).isEqualTo(VehicleStatus.available);
  }

  @Test
  void fromCode_whenKnownCode_thenReturnsMatchingEnum() {
    assertThat(VehicleStatus.fromCode("MAINTENANCE")).isEqualTo(VehicleStatus.maintenance);
    assertThat(VehicleStatus.fromCode("rented")).isEqualTo(VehicleStatus.rented);
  }
}
