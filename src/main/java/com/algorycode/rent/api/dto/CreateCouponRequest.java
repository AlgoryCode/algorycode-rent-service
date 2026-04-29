package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateCouponRequest(
    @NotBlank @Size(max = 64) String code,
    @NotBlank @Size(max = 16) String discountType,
    @NotNull @DecimalMin("0") BigDecimal discountValue,
    @Size(max = 255) String description,
    Boolean active,
    Integer usageLimit,
    Instant expiresAt) {}
