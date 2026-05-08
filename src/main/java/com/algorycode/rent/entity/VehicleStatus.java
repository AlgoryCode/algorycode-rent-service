package com.algorycode.rent.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum VehicleStatus {
  active(1L, "active", "available"),
  pending(2L, "pending"),
  maintenance(3L, "maintenance", "in_repair"),
  rented(4L, "rented");

  private final long stableId;
  private final String[] dbLookupCodes;

  VehicleStatus(long stableId, String... dbLookupCodes) {
    this.stableId = stableId;
    this.dbLookupCodes = dbLookupCodes;
  }

  public long getStableId() {
    return stableId;
  }

  public String persistenceCode() {
    return dbLookupCodes[0];
  }

  public String[] dbLookupCodes() {
    return dbLookupCodes.clone();
  }

  private static final Map<String, VehicleStatus> PARSE_INDEX = new HashMap<>();

  static {
    for (VehicleStatus v : values()) {
      register(v.name(), v);
      for (String code : v.dbLookupCodes) {
        register(code, v);
      }
    }
    register("aktif", active);
    register("beklemede", pending);
    register("tamirde", maintenance);
    register("tamir", maintenance);
    register("kirada", rented);
  }

  private static void register(String key, VehicleStatus v) {
    if (key == null || key.isBlank()) {
      return;
    }
    String collapsed = key.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      PARSE_INDEX.putIfAbsent(n, v);
      PARSE_INDEX.putIfAbsent(n.replace(" ", ""), v);
    }
  }

  public static Optional<VehicleStatus> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String collapsed = raw.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      VehicleStatus v = PARSE_INDEX.get(n);
      if (v != null) {
        return Optional.of(v);
      }
      v = PARSE_INDEX.get(n.replace(" ", ""));
      if (v != null) {
        return Optional.of(v);
      }
    }
    return Optional.empty();
  }

  public static VehicleStatus parseRequired(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("invalid vehicle status: " + raw));
  }

  public static VehicleStatus fromDbCode(String code) {
    return tryParse(code).orElse(active);
  }

  @Deprecated
  public static VehicleStatus fromCode(String code) {
    return fromDbCode(code);
  }
}
