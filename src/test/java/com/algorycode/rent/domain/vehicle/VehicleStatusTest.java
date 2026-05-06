package com.algorycode.rent.domain.vehicle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VehicleStatusTest {

  @Test
  void fromDbCode_whenUnknownCode_thenReturnsActive() {
    assertThat(VehicleStatus.fromDbCode("custom_status")).isEqualTo(VehicleStatus.active);
  }

  @Test
  void fromDbCode_whenKnownCode_thenReturnsMatchingEnum() {
    assertThat(VehicleStatus.fromDbCode("MAINTENANCE")).isEqualTo(VehicleStatus.maintenance);
    assertThat(VehicleStatus.fromDbCode("rented")).isEqualTo(VehicleStatus.rented);
    assertThat(VehicleStatus.fromDbCode("available")).isEqualTo(VehicleStatus.active);
  }

  @Test
  void tryParse_acceptsFeAliases() {
    assertThat(VehicleStatus.tryParse("Active")).contains(VehicleStatus.active);
    assertThat(VehicleStatus.tryParse("Tamirde")).contains(VehicleStatus.maintenance);
    assertThat(VehicleStatus.tryParse("pending")).contains(VehicleStatus.pending);
  }
}
