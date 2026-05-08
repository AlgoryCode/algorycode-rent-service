package com.algorycode.rent.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record UpdateCouponRequest(
    @Size(max = 64) String code,
    @Size(max = 16) String discountType,
    @DecimalMin("0") BigDecimal discountValue,
    @Size(max = 255) String description,
    Boolean active,
    Integer usageLimit,
    Instant expiresAt) {}
