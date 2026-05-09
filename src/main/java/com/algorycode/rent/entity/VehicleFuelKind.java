package com.algorycode.rent.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum VehicleFuelKind {
  gasoline(1, 1, "Benzin", "gasoline", "petrol"),
  diesel(2, 2, "Dizel", "diesel"),
  electric(3, 3, "Elektrik (BEV)", "electric", "ev", "bev"),
  hybrid(4, 4, "Hibrit (HEV)", "hybrid", "hev"),
  plug_in_hybrid(
      5, 5, "Takılabilir hibrit (PHEV)", "plug_in_hybrid", "pluginhybrid", "phev", "plug-in hybrid"),
  lpg(6, 6, "LPG", "lpg"),
  cng(7, 7, "CNG", "cng");

  private final int stableId;
  private final int sortOrder;
  private final String labelTr;
  private final String[] dbLookupCodes;

  VehicleFuelKind(int stableId, int sortOrder, String labelTr, String... dbLookupCodes) {
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

  private static final Map<String, VehicleFuelKind> PARSE_INDEX = new HashMap<>();

  static {
    for (VehicleFuelKind v : values()) {
      register(v.name(), v);
      for (String code : v.dbLookupCodes) {
        register(code, v);
      }
    }
    register("benzin", gasoline);
    register("dizel", diesel);
  }

  private static void register(String key, VehicleFuelKind v) {
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

  public static Optional<VehicleFuelKind> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String collapsed = raw.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      VehicleFuelKind v = PARSE_INDEX.get(n);
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

  public static VehicleFuelKind parseRequired(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("invalid fuel type: " + raw));
  }
}
