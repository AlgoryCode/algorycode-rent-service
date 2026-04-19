package com.algorycode.rent.service.support;

/**
 * Ortak string normalizasyonu (DRY). Semantik:
 *
 * <ul>
 *   <li>{@link #trimOrNull} — boş/blank → {@code null}; aksi halde trim (opsiyonel alanlar).
 *   <li>{@link #cleanOrNull} — {@link #trimOrNull} ile aynı (eski {@code cleanOrNull} adıyla uyum).
 *   <li>{@link #blankToEmpty} — {@code null} veya yalnızca boşluk → {@code ""}; aksi halde trim (zorunlu metin
 *       alanlarında boş string bekleniyorsa).
 * </ul>
 */
public final class Text {

  private Text() {}

  /** Boş veya yalnızca boşluk → {@code null}; aksi halde trim. */
  public static String trimOrNull(String s) {
    if (s == null || s.isBlank()) {
      return null;
    }
    return s.trim();
  }

  /** {@link #trimOrNull} ile aynı. */
  public static String cleanOrNull(String input) {
    return trimOrNull(input);
  }

  /** {@code null} veya blank → {@code ""}; aksi halde trim. */
  public static String blankToEmpty(String s) {
    return s == null || s.isBlank() ? "" : s.trim();
  }
}
