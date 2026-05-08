package com.algorycode.rent.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum RentalStatus {
  active(1L, "active"),
  pending(2L, "pending"),
  cancelled(3L, "cancelled"),
  completed(4L, "completed");

  private final long stableId;
  private final String[] dbLookupCodes;

  RentalStatus(long stableId, String... dbLookupCodes) {
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

  private static final Map<String, RentalStatus> PARSE_INDEX = new HashMap<>();

  static {
    for (RentalStatus s : values()) {
      register(s.name(), s);
      for (String code : s.dbLookupCodes) {
        register(code, s);
      }
    }
    register("aktif", active);
    register("onay bekliyor", pending);
    register("onaybekliyor", pending);
    register("beklemede", pending);
    register("iptal", cancelled);
    register("tamamlandı", completed);
    register("tamamlandi", completed);
    register("bitti", completed);
  }

  private static void register(String key, RentalStatus s) {
    if (key == null || key.isBlank()) {
      return;
    }
    String collapsed = key.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      PARSE_INDEX.putIfAbsent(n, s);
      PARSE_INDEX.putIfAbsent(n.replace(" ", ""), s);
    }
  }

  public static Optional<RentalStatus> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String collapsed = raw.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      RentalStatus s = PARSE_INDEX.get(n);
      if (s != null) {
        return Optional.of(s);
      }
      s = PARSE_INDEX.get(n.replace(" ", ""));
      if (s != null) {
        return Optional.of(s);
      }
    }
    return Optional.empty();
  }

  public static RentalStatus parseRequired(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("invalid rental status: " + raw));
  }

  public static RentalStatus fromDbCode(String code) {
    return tryParse(code).orElse(active);
  }

  @Deprecated
  public static RentalStatus fromCode(String code) {
    return fromDbCode(code);
  }
}
