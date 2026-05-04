package com.algorycode.rent.domain.vehicle;

import java.util.Locale;

public enum VehicleStatus {
  available,
  maintenance,
  rented;

  public static VehicleStatus fromCode(String code) {
    if (code == null || code.isBlank()) {
      return available;
    }
    String normalized = code.trim().toLowerCase(Locale.ROOT);
    try {
      return valueOf(normalized);
    } catch (IllegalArgumentException ex) {
      return available;
    }
  }
}
