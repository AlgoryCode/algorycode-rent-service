package com.algorycode.rent.service.support;

import com.algorycode.rent.entity.Vehicle;
import com.algorycode.rent.entity.VehicleBrand;
import com.algorycode.rent.entity.VehicleModel;
import com.algorycode.rent.entity.VehicleStatus;

public final class VehicleTestFixtures {

  private VehicleTestFixtures() {}

  public static void attachBrandModelStatus(
      Vehicle v, String brand, String model, VehicleStatus status) {
    if (v.getPlate() == null || v.getPlate().isBlank()) {
      v.setPlate("34 TEST 01");
    }
    VehicleBrand b = new VehicleBrand();
    b.setId(901L);
    b.setName(brand);
    VehicleModel m = new VehicleModel();
    m.setId(902L);
    m.setBrandId(b.getId());
    m.setBrand(b);
    m.setName(model);
    v.setVehicleModelId(m.getId());
    v.setVehicleModel(m);
    v.setVehicleStatus(status);
  }
}
