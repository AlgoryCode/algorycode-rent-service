package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.HandoverPricingQuoteDto;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.repository.HandoverLocationRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HandoverPricingService {

  private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

  private final HandoverLocationRepository handoverLocationRepository;

  /**
   * Alış ve iade noktası aynı kayıt değilse: her iki noktanın {@code surcharge_eur} toplamı +
   * bilinen ülke çifti güzergâh ücreti (ör. AL→XK 60 €, AL→ME 75 €). Aynı nokta veya eksik ülke
   * kodunda güzergâh 0.
   */
  @Transactional(readOnly = true)
  public HandoverPricingQuoteDto quote(Long pickupHandoverId, Long returnHandoverId) {
    if (pickupHandoverId == null
        || returnHandoverId == null
        || pickupHandoverId.equals(returnHandoverId)) {
      return new HandoverPricingQuoteDto(ZERO, ZERO, ZERO, ZERO, false);
    }
    HandoverLocation pickup =
        handoverLocationRepository
            .findById(pickupHandoverId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Alış noktası bulunamadı: " + pickupHandoverId));
    HandoverLocation ret =
        handoverLocationRepository
            .findById(returnHandoverId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Teslim noktası bulunamadı: " + returnHandoverId));

    BigDecimal legP = nz(pickup.getSurchargeEur());
    BigDecimal legR = nz(ret.getSurchargeEur());
    BigDecimal route = resolveRouteEur(pickup, ret).setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = legP.add(legR).add(route).setScale(2, RoundingMode.HALF_UP);
    boolean applied = total.compareTo(ZERO) > 0;
    return new HandoverPricingQuoteDto(legP, legR, route, total, applied);
  }

  public HandoverPricingQuoteDto quoteForPersistedPair(
      HandoverLocation pickup, HandoverLocation returnLoc) {
    if (pickup == null || returnLoc == null) {
      return new HandoverPricingQuoteDto(ZERO, ZERO, ZERO, ZERO, false);
    }
    if (pickup.getId().equals(returnLoc.getId())) {
      return new HandoverPricingQuoteDto(ZERO, ZERO, ZERO, ZERO, false);
    }
    HandoverLocation p = handoverLocationRepository.findById(pickup.getId()).orElse(pickup);
    HandoverLocation r = handoverLocationRepository.findById(returnLoc.getId()).orElse(returnLoc);
    BigDecimal legP = nz(p.getSurchargeEur());
    BigDecimal legR = nz(r.getSurchargeEur());
    BigDecimal route = resolveRouteEur(p, r).setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = legP.add(legR).add(route).setScale(2, RoundingMode.HALF_UP);
    boolean applied = total.compareTo(ZERO) > 0;
    return new HandoverPricingQuoteDto(legP, legR, route, total, applied);
  }

  private static BigDecimal nz(BigDecimal v) {
    if (v == null) {
      return ZERO;
    }
    return v.setScale(2, RoundingMode.HALF_UP);
  }

  private static BigDecimal resolveRouteEur(HandoverLocation pickup, HandoverLocation returnLoc) {
    String pc = normalizeCountryCode(pickup.getCountryCode());
    String rc = normalizeCountryCode(returnLoc.getCountryCode());
    if (pc == null || rc == null || pc.equals(rc)) {
      return ZERO;
    }
    if (pair(pc, rc, "AL", "XK") || pair(pc, rc, "XK", "AL")) {
      return BigDecimal.valueOf(60);
    }
    if (pair(pc, rc, "AL", "ME") || pair(pc, rc, "ME", "AL")) {
      return BigDecimal.valueOf(75);
    }
    return ZERO;
  }

  private static boolean pair(String a, String b, String x, String y) {
    return (x.equals(a) && y.equals(b)) || (x.equals(b) && y.equals(a));
  }

  private static String normalizeCountryCode(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return raw.trim().toUpperCase(java.util.Locale.ROOT);
  }
}
