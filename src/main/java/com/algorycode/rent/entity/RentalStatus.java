package com.algorycode.rent.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public enum RentalStatus {
  ACTIVE,
  PENDING,
  CANCELLED,
  COMPLETED;

  private static final Map<String, RentalStatus> SYNONYMS = new HashMap<>();

  static {
    putSynonyms("active", ACTIVE);
    putSynonyms("pending", PENDING);
    putSynonyms("cancelled", CANCELLED);
    putSynonyms("canceled", CANCELLED);
    putSynonyms("completed", COMPLETED);
    putSynonyms("aktif", ACTIVE);
    putSynonyms("beklemede", PENDING);
    putSynonyms("onay bekliyor", PENDING);
    putSynonyms("onaybekliyor", PENDING);
    putSynonyms("iptal", CANCELLED);
    putSynonyms("tamamlandı", COMPLETED);
    putSynonyms("tamamlandi", COMPLETED);
    putSynonyms("bitti", COMPLETED);
  }

  private static void putSynonyms(String key, RentalStatus status) {
    if (key == null || key.isBlank()) {
      return;
    }
    String collapsed = key.trim().replaceAll("\\s+", " ");
    for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
      String n = collapsed.toLowerCase(loc);
      SYNONYMS.putIfAbsent(n, status);
      SYNONYMS.putIfAbsent(n.replace(" ", ""), status);
    }
  }

  public static Optional<RentalStatus> tryParse(String raw) {
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    String collapsed = raw.trim().replaceAll("\\s+", " ");
    try {
      return Optional.of(valueOf(collapsed.toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException ex) {
      for (Locale loc : new Locale[] {Locale.forLanguageTag("tr"), Locale.ROOT}) {
        String n = collapsed.toLowerCase(loc);
        RentalStatus s = SYNONYMS.get(n);
        if (s != null) {
          return Optional.of(s);
        }
        s = SYNONYMS.get(n.replace(" ", ""));
        if (s != null) {
          return Optional.of(s);
        }
      }
      return Optional.empty();
    }
  }

  public static RentalStatus parseRequired(String raw) {
    return tryParse(raw)
        .orElseThrow(() -> new IllegalArgumentException("invalid rental status: " + raw));
  }

  public static RentalStatus fromDbCode(String code) {
    return tryParse(code).orElse(ACTIVE);
  }
}
