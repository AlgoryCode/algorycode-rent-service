package com.algorycode.rent.service.support;

import com.algorycode.rent.api.error.BadRequestException;

public final class VehicleCatalogSupport {

  private VehicleCatalogSupport() {}

  public static void requireUpdateHasSomething(String labelTr, Integer sortOrder) {
    boolean hasLabel = labelTr != null && !labelTr.isBlank();
    if (!hasLabel && sortOrder == null) {
      throw new BadRequestException("Güncelleme için labelTr veya sortOrder verilmelidir.");
    }
  }

  public static String normalizeBodyStyleCode(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException("Kod boş olamaz.");
    }
    return raw.trim().toUpperCase();
  }

  public static String normalizeFuelOrTransmissionCode(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException("Kod boş olamaz.");
    }
    return raw.trim().toLowerCase();
  }
}
