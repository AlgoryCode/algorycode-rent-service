package com.algorycode.rent.repository;

public interface VehicleSnapshotRow {
  Long getId();

  String getPlate();

  Integer getYear();

  String getStatusCode();

  Boolean getExternal();

  java.math.BigDecimal getRentalDailyPrice();

  String getCountryCode();

  String getSnapshotText();
}
