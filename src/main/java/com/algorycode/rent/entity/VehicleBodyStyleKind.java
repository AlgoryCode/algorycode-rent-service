package com.algorycode.rent.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum VehicleBodyStyleKind {
  sedan(1, 1, "Sedan", "sedan"),
  hatchback(2, 2, "Hatchback", "hatchback"),
  station_wagon(3, 3, "Station wagon", "station_wagon", "wagon", "estate"),
  suv(4, 4, "SUV / crossover", "suv", "crossover"),
  coupe(5, 5, "Coupe", "coupe"),
  convertible(6, 6, "Cabrio / convertible", "convertible", "cabrio"),
  mpv(7, 7, "MPV / minivan", "mpv", "minivan"),
  van(8, 8, "Van / panelvan", "van", "panelvan"),
  pickup(9, 9, "Pick-up", "pickup", "pick-up"),
  roadster(10, 10, "Roadster", "roadster");

  private final int stableId;
  private final int sortOrder;
  private final String labelTr;
  private final String[] dbLookupCodes;

  VehicleBodyStyleKind(int stableId, int sortOrder, String labelTr, String... dbLookupCodes) {
    this.stableId = stableId;
    this.sortOrder = sortOrder;
    this.labelTr = labelTr;
    this.dbLookupCodes = dbLookupCodes;
  }

  public int getStableId() {
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

  private static final Map<String, VehicleBodyStyleKind> PARSE_INDEX = new HashMap<>();

  static {
    for (VehicleBodyStyleKind v : values()) {
      register(v.name(), v);
      for (String code : v.dbLookupCodes) {
        register(code, v);
      }
    }
  }

  private static void register(String key, VehicleBodyStyleKind v) {
    if (key == null || key.isBlank()) {
      return;
    }
    String collapsed = key.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      PARSE_INDEX.putIfAbsent(n, v);
      PARSE_INDEX.putIfAbsent(n.replace(" ", ""), v);
      PARSE_INDEX.putIfAbsent(n.replace("_", ""), v);
      PARSE_INDEX.putIfAbsent(n.replace("-", ""), v);
    }
  }

  public static Optional<VehicleBodyStyleKind> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String collapsed = raw.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      VehicleBodyStyleKind v = PARSE_INDEX.get(n);
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

  public static VehicleBodyStyleKind parseRequired(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("invalid body style: " + raw));
  }
}
