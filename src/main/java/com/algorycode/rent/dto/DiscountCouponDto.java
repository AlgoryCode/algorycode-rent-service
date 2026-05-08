package com.algorycode.rent.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DiscountCouponDto(
    Long id,
    String code,
    String discountType,
    BigDecimal discountValue,
    String description,
    boolean active,
    Integer usageLimit,
    int usageCount,
    Instant expiresAt,
    Instant createdAt,
    Instant updatedAt) {}
