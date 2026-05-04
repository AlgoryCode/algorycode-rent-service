package com.algorycode.rent.service.support;

import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ConflictException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

public final class VehicleCatalogSupport {

  private VehicleCatalogSupport() {}

  public static void requireUpdateHasSomething(String labelTr, Integer sortOrder) {
    boolean hasLabel = labelTr != null && !labelTr.isBlank();
    if (!hasLabel && sortOrder == null) {
      throw new BadRequestException("Güncelleme için özellik adı veya sıra no verilmelidir.");
    }
  }

  public static String normalizeBodyStyleCode(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException("Kod boş olamaz.");
    }
    return raw.trim().toUpperCase(Locale.ROOT);
  }

  public static String normalizeFuelOrTransmissionCode(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new BadRequestException("Kod boş olamaz.");
    }
    return raw.trim().toLowerCase(Locale.ROOT);
  }

  /** Özellik adından veritabanı kodu türetir (harf/rakam ve alt çizgi). Boş kalırsa hata. */
  public static String slugCodeFromLabel(String labelTr) {
    if (labelTr == null || labelTr.isBlank()) {
      throw new BadRequestException("Özellik adı boş olamaz.");
    }
    String lower = labelTr.trim().toLowerCase(Locale.forLanguageTag("tr-TR"));
    String ascii =
        Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT);
    String s = ascii.replaceAll("[^a-z0-9]+", "_");
    s = s.replaceAll("_+", "_").replaceAll("^_|_$", "");
    if (s.isEmpty()) {
      throw new BadRequestException(
          "Özellik adından geçerli bir kod türetilemedi; harf veya rakam içeren bir ad girin.");
    }
    return s.length() > 32 ? s.substring(0, 32) : s;
  }

  /**
   * İsteğe bağlı kullanıcı kodu veya etiketten türetilen taban + sayısal sonek ile benzersiz kod.
   *
   * @param upperCaseConvention {@code true} araç türü (büyük harf), {@code false} yakıt/vites
   *     (küçük harf)
   */
  public static String resolveNewCatalogCode(
      String optionalUserCode,
      String labelTr,
      boolean upperCaseConvention,
      Predicate<String> existsIgnoreCase) {
    if (optionalUserCode != null && !optionalUserCode.isBlank()) {
      String normalized =
          upperCaseConvention
              ? normalizeBodyStyleCode(optionalUserCode.trim())
              : normalizeFuelOrTransmissionCode(optionalUserCode.trim());
      if (existsIgnoreCase.test(normalized)) {
        throw new ConflictException("Bu kod zaten kayıtlı: " + normalized);
      }
      return normalized;
    }
    String slug = slugCodeFromLabel(labelTr);
    String base = upperCaseConvention ? slug.toUpperCase(Locale.ROOT) : slug;
    return allocateUniqueCode(base, existsIgnoreCase);
  }

  static String allocateUniqueCode(String base, Predicate<String> existsIgnoreCase) {
    String candidate = base.length() > 32 ? base.substring(0, 32) : base;
    if (!existsIgnoreCase.test(candidate)) {
      return candidate;
    }
    for (int n = 2; n < 10_000; n++) {
      String suffix = "_" + n;
      int keep = Math.max(1, 32 - suffix.length());
      String shortened = base.length() > keep ? base.substring(0, keep) : base;
      candidate =
          (shortened + suffix).length() > 32
              ? (shortened + suffix).substring(0, 32)
              : shortened + suffix;
      if (!existsIgnoreCase.test(candidate)) {
        return candidate;
      }
    }
    throw new ConflictException("Benzersiz kod üretilemedi; farklı bir özellik adı deneyin.");
  }
}
