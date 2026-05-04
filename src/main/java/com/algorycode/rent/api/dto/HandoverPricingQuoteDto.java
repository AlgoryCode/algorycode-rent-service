package com.algorycode.rent.api.dto;

import java.math.BigDecimal;

/**
 * Farklı teslim (alış ≠ iade noktası) için: nokta başı ek ücretler + ülke çifti güzergâh ücreti
 * (EUR). Aynı noktada alış/iade veya eksik coğrafya: toplam 0.
 */
public record HandoverPricingQuoteDto(
    BigDecimal pickupLegEur,
    BigDecimal returnLegEur,
    BigDecimal routeEur,
    BigDecimal totalEur,
    boolean applied) {}
