package com.algorycode.rent.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * KVKK: e-posta, telefon, ad-soyad, TC, pasaport, ham exception metni, data URL gövdesi loglanmaz.
 * Yalnızca whitelist anahtarları ve güvenli değerler (UUID, ISO tarih, enum adı, referans kodu).
 */
@Component
public class AuditLog {

  private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

  private static final Set<String> ALLOWED_KEYS =
      Set.of(
          "event",
          "reasonCode",
          "vehicleId",
          "rentalId",
          "rentalRequestId",
          "userId",
          "status",
          "blockingRentalId",
          "overlapStart",
          "overlapEnd",
          "requestedStart",
          "requestedEnd",
          "httpStatus",
          "exceptionType",
          "fieldErrorCount",
          "referenceNo",
          "entity");

  public void infoEvent(String event, Map<String, String> dimensions) {
    logWithLevel("INFO", event, dimensions);
  }

  public void warnBusiness(String reasonCode, Map<String, String> dimensions) {
    Map<String, String> m = new HashMap<>(dimensions);
    m.putIfAbsent("reasonCode", reasonCode);
    logWithLevel("WARN", reasonCode, m);
  }

  /** Beklenmeyen teknik hata: mesaj gövdesi loglanmaz, yalnızca sınıf adı. */
  public void errorTechnical(String event, Throwable t, Map<String, String> dimensions) {
    String type = t == null ? "null" : t.getClass().getName();
    Map<String, String> m = new HashMap<>(dimensions);
    m.put("exceptionType", type);
    logWithLevel("ERROR", event, m);
  }

  private void logWithLevel(String level, String primary, Map<String, String> rawDimensions) {
    Map<String, String> safe = sanitize(rawDimensions);
    safe.put("event", primary);
    for (var e : safe.entrySet()) {
      if (e.getValue() != null) {
        MDC.put("audit." + e.getKey(), e.getValue());
      }
    }
    try {
      String line = safe.entrySet().stream().map(en -> en.getKey() + "=" + en.getValue()).collect(Collectors.joining(" "));
      switch (level) {
        case "WARN" -> AUDIT.warn("{}", line);
        case "ERROR" -> AUDIT.error("{}", line);
        default -> AUDIT.info("{}", line);
      }
    } finally {
      for (String k : safe.keySet()) {
        MDC.remove("audit." + k);
      }
    }
  }

  private static Map<String, String> sanitize(Map<String, String> in) {
    if (in == null || in.isEmpty()) {
      return new HashMap<>();
    }
    Map<String, String> out = new HashMap<>();
    for (var e : in.entrySet()) {
      String k = e.getKey();
      if (k == null || !ALLOWED_KEYS.contains(k)) {
        continue;
      }
      String v = e.getValue();
      if (v == null) {
        continue;
      }
      String t = v.trim();
      if (t.length() > 256) {
        t = t.substring(0, 256);
      }
      out.put(k, t);
    }
    return out;
  }
}
