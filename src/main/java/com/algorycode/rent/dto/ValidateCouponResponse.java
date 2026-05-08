package com.algorycode.rent.dto;

import java.math.BigDecimal;

public record ValidateCouponResponse(
    boolean valid, String discountType, BigDecimal discountValue, String message) {}
