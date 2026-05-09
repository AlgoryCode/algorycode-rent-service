package com.algorycode.rent.entity;

import java.util.Locale;

public enum VehicleStatus {
  ACTIVE,
  PENDING,
  MAINTENANCE,
  RENTED;

  public static VehicleStatus fromCatalogCode(String code) {
    if (code == null || code.isBlank()) {
      return ACTIVE;
    }
    String k = code.trim().toLowerCase(Locale.ROOT);
    return switch (k) {
      case "rented" -> RENTED;
      case "available", "active" -> ACTIVE;
      case "maintenance", "in_repair" -> MAINTENANCE;
      case "pending", "reserved" -> PENDING;
      default ->
          resolveStrict(code.trim());
    };
  }

  private static VehicleStatus resolveStrict(String raw) {
    try {
      return VehicleStatus.valueOf(raw.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return ACTIVE;
    }
  }
}
