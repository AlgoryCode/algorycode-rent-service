package com.algorycode.rent.service.vehiclecatalog;

import com.algorycode.rent.entity.VehicleBodyStyle;
import com.algorycode.rent.entity.VehicleFuelType;
import com.algorycode.rent.entity.VehicleStatusCatalog;
import com.algorycode.rent.entity.VehicleTransmissionType;

public final class VehicleCatalogEntityFactory {

  private VehicleCatalogEntityFactory() {}

  public static VehicleBodyStyle newBodyStyle(String code, String labelTr, int sortOrder) {
    VehicleBodyStyle e = new VehicleBodyStyle();
    e.setCode(code);
    e.setLabelTr(labelTr);
    e.setSortOrder(sortOrder);
    return e;
  }

  public static VehicleFuelType newFuelType(String code, String labelTr, int sortOrder) {
    VehicleFuelType e = new VehicleFuelType();
    e.setCode(code);
    e.setLabelTr(labelTr);
    e.setSortOrder(sortOrder);
    return e;
  }

  public static VehicleTransmissionType newTransmissionType(
      String code, String labelTr, int sortOrder) {
    VehicleTransmissionType e = new VehicleTransmissionType();
    e.setCode(code);
    e.setLabelTr(labelTr);
    e.setSortOrder(sortOrder);
    return e;
  }

  public static VehicleStatusCatalog newVehicleStatusCatalog(
      String code, String labelTr, int sortOrder) {
    VehicleStatusCatalog e = new VehicleStatusCatalog();
    e.setCode(code);
    e.setLabelTr(labelTr);
    e.setSortOrder(sortOrder);
    return e;
  }
}
