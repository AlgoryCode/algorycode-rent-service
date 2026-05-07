package com.algorycode.rent.service.support;

import com.algorycode.rent.domain.vehicle.Vehicle;
import com.algorycode.rent.domain.vehicle.VehicleBrand;
import com.algorycode.rent.domain.vehicle.VehicleModel;
import com.algorycode.rent.domain.vehicle.VehicleStatus;
import com.algorycode.rent.domain.vehicle.VehicleStatusCatalog;

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
    VehicleStatusCatalog d = new VehicleStatusCatalog();
    d.setId(903L);
    d.setCode(status.name());
    v.setVehicleStatusId(d.getId());
    v.setVehicleStatus(d);
  }
}
