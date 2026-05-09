package com.algorycode.rent.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VehicleStatusTest {

  @Test
  void fromCatalogCode_blank_thenActive() {
    assertThat(VehicleStatus.fromCatalogCode(null)).isEqualTo(VehicleStatus.ACTIVE);
    assertThat(VehicleStatus.fromCatalogCode("   ")).isEqualTo(VehicleStatus.ACTIVE);
  }

  @Test
  void fromCatalogCode_catalogSynonyms() {
    assertThat(VehicleStatus.fromCatalogCode("rented")).isEqualTo(VehicleStatus.RENTED);
    assertThat(VehicleStatus.fromCatalogCode("available")).isEqualTo(VehicleStatus.ACTIVE);
    assertThat(VehicleStatus.fromCatalogCode("active")).isEqualTo(VehicleStatus.ACTIVE);
    assertThat(VehicleStatus.fromCatalogCode("maintenance")).isEqualTo(VehicleStatus.MAINTENANCE);
    assertThat(VehicleStatus.fromCatalogCode("pending")).isEqualTo(VehicleStatus.PENDING);
  }

  @Test
  void fromCatalogCode_strictEnumName() {
    assertThat(VehicleStatus.fromCatalogCode("RENTED")).isEqualTo(VehicleStatus.RENTED);
    assertThat(VehicleStatus.fromCatalogCode("ACTIVE")).isEqualTo(VehicleStatus.ACTIVE);
  }

  @Test
  void fromCatalogCode_unknown_thenActive() {
    assertThat(VehicleStatus.fromCatalogCode("no_such_status")).isEqualTo(VehicleStatus.ACTIVE);
  }
}
