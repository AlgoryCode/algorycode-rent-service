package com.algorycode.rent.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class VehicleBodyStyleKindTest {

  @Test
  void tryParse_whenSuvAlias_thenSuv() {
    assertThat(VehicleBodyStyleKind.tryParse("crossover")).contains(VehicleBodyStyleKind.suv);
  }

  @Test
  void stableId_matchesCatalogSeedOrder() {
    assertThat(VehicleBodyStyleKind.sedan.getStableId()).isEqualTo(1);
    assertThat(VehicleBodyStyleKind.roadster.getStableId()).isEqualTo(10);
  }

  @Test
  void parseRequired_whenUnknown_thenThrows() {
    assertThatThrownBy(() -> VehicleBodyStyleKind.parseRequired("not-a-body"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
