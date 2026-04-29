package com.algorycode.rent.domain.vehicle;

public enum VehicleStatus {
  available,
  maintenance,
  rented;

  public static VehicleStatus fromCode(String code) {
    if (code == null || code.isBlank()) {
      return available;
    }
    return valueOf(code.trim().toLowerCase());
  }
}
