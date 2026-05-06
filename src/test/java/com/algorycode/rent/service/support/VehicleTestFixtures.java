package com.algorycode.rent.service.support;

import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleBrand;
import com.algorycode.rent.domain.vehicle.VehicleModel;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.domain.vehicle.VehicleStatusDefinition;

public final class VehicleTestFixtures {

  private VehicleTestFixtures() {}

  public static void attachBrandModelStatus(
      Vehicle v, String brand, String model, VehicleStatus status) {
    if (v.getPlate() == null || v.getPlate().isBlank()) {
      v.setPlate("34 TEST 01");
    }
    VehicleBrand b = new VehicleBrand();
    b.setName(brand);
    VehicleModel m = new VehicleModel();
    m.setBrand(b);
    m.setName(model);
    v.setVehicleModel(m);
    VehicleStatusDefinition d = new VehicleStatusDefinition();
    d.setCode(status.name());
    v.setStatusDefinition(d);
  }
}
