package com.algorycode.rent.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum VehicleTransmissionKind {
  manual(1L, 1, "Manuel", "manual"),
  automatic(2L, 2, "Otomatik", "automatic"),
  semi_automatic(3L, 3, "Yarı otomatik", "semi_automatic", "semi-automatic", "semi automatic");

  private final long stableId;
  private final int sortOrder;
  private final String labelTr;
  private final String[] dbLookupCodes;

  VehicleTransmissionKind(long stableId, int sortOrder, String labelTr, String... dbLookupCodes) {
    this.stableId = stableId;
    this.sortOrder = sortOrder;
    this.labelTr = labelTr;
    this.dbLookupCodes = dbLookupCodes;
  }

  public long getStableId() {
    return stableId;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public String getLabelTr() {
    return labelTr;
  }

  public String persistenceCode() {
    return dbLookupCodes[0];
  }

  public String[] dbLookupCodes() {
    return dbLookupCodes.clone();
  }

  private static final Map<String, VehicleTransmissionKind> PARSE_INDEX = new HashMap<>();

  static {
    for (VehicleTransmissionKind v : values()) {
      register(v.name(), v);
      for (String code : v.dbLookupCodes) {
        register(code, v);
      }
    }
    register("manuel", manual);
    register("otomatik", automatic);
    register("yarı otomatik", semi_automatic);
    register("yarıotomatik", semi_automatic);
  }

  private static void register(String key, VehicleTransmissionKind v) {
    if (key == null || key.isBlank()) {
      return;
    }
    String collapsed = key.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      PARSE_INDEX.putIfAbsent(n, v);
      PARSE_INDEX.putIfAbsent(n.replace(" ", ""), v);
      PARSE_INDEX.putIfAbsent(n.replace("_", ""), v);
    }
  }

  public static Optional<VehicleTransmissionKind> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String collapsed = raw.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      VehicleTransmissionKind v = PARSE_INDEX.get(n);
      if (v != null) {
        return Optional.of(v);
      }
      v = PARSE_INDEX.get(n.replace(" ", ""));
      if (v != null) {
        return Optional.of(v);
      }
      v = PARSE_INDEX.get(n.replace("_", ""));
      if (v != null) {
        return Optional.of(v);
      }
    }
    return Optional.empty();
  }

  public static VehicleTransmissionKind parseRequired(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("invalid transmission type: " + raw));
  }
}
