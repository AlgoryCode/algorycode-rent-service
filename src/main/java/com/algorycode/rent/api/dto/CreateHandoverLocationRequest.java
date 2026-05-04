package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.location.HandoverLocationKind;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateHandoverLocationRequest(
    HandoverLocationKind kind,
    @Size(max = 255) String name,
    @Size(max = 4000) String description,
    @Size(max = 500) String addressLine,
    @Size(max = 64) String countryCode,
    /** {@code null} veya {@code true}: aktif; yalnızca {@code false} pasif kayıt oluşturur. */
    Boolean active,
    int lineOrder,
    /** Alış/teslim rolünde bu nokta seçildiğinde eklenecek ek ücret (EUR); {@code null} → 0. */
    BigDecimal surchargeEur) {}
