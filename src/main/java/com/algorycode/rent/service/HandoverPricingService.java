package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.HandoverPricingQuoteDto;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.domain.country.Country;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.repository.HandoverLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class HandoverPricingService {

  private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

  private final HandoverLocationRepository handoverLocationRepository;

  public HandoverPricingService(HandoverLocationRepository handoverLocationRepository) {
    this.handoverLocationRepository = handoverLocationRepository;
  }

  /**
   * Alış ve iade noktası aynı kayıt değilse: her iki noktanın {@code surcharge_eur} toplamı + bilinen ülke çifti
   * güzergâh ücreti (ör. AL→XK 60 €, AL→ME 75 €). Aynı UUID veya eksik ülke kodunda güzergâh 0.
   */
  @Transactional(readOnly = true)
  public HandoverPricingQuoteDto quote(java.util.UUID pickupHandoverId, java.util.UUID returnHandoverId) {
    if (pickupHandoverId == null || returnHandoverId == null || pickupHandoverId.equals(returnHandoverId)) {
      return new HandoverPricingQuoteDto(ZERO, ZERO, ZERO, ZERO, false);
    }
    HandoverLocation pickup =
        handoverLocationRepository
            .findByIdWithCityAndCountry(pickupHandoverId)
            .orElseThrow(() -> new ResourceNotFoundException("Alış noktası bulunamadı: " + pickupHandoverId));
    HandoverLocation ret =
        handoverLocationRepository
            .findByIdWithCityAndCountry(returnHandoverId)
            .orElseThrow(() -> new ResourceNotFoundException("Teslim noktası bulunamadı: " + returnHandoverId));

    BigDecimal legP = nz(pickup.getSurchargeEur());
    BigDecimal legR = nz(ret.getSurchargeEur());
    BigDecimal route = resolveRouteEur(pickup, ret).setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = legP.add(legR).add(route).setScale(2, RoundingMode.HALF_UP);
    boolean applied = total.compareTo(ZERO) > 0;
    return new HandoverPricingQuoteDto(legP, legR, route, total, applied);
  }

  public HandoverPricingQuoteDto quoteForPersistedPair(HandoverLocation pickup, HandoverLocation returnLoc) {
    if (pickup == null || returnLoc == null) {
      return new HandoverPricingQuoteDto(ZERO, ZERO, ZERO, ZERO, false);
    }
    if (pickup.getId().equals(returnLoc.getId())) {
      return new HandoverPricingQuoteDto(ZERO, ZERO, ZERO, ZERO, false);
    }
    HandoverLocation p =
        handoverLocationRepository.findByIdWithCityAndCountry(pickup.getId()).orElse(pickup);
    HandoverLocation r =
        handoverLocationRepository.findByIdWithCityAndCountry(returnLoc.getId()).orElse(returnLoc);
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

  private BigDecimal resolveRouteEur(HandoverLocation pickup, HandoverLocation returnLoc) {
    String pc = countryCode(pickup.getCity());
    String rc = countryCode(returnLoc.getCity());
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

  private static String countryCode(City city) {
    if (city == null) {
      return null;
    }
    Country c = city.getCountry();
    return c != null ? c.getCode() : null;
  }
}
