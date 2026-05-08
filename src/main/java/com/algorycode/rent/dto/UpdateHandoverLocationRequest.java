package com.algorycode.rent.dto;

import com.algorycode.rent.entity.HandoverLocationKind;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateHandoverLocationRequest(
    HandoverLocationKind kind,
    @Size(max = 255) String name,
    @Size(max = 4000) String description,
    @Size(max = 500) String addressLine,
    @Size(max = 64) String countryCode,
    Boolean active,
    Integer lineOrder,
    BigDecimal surchargeEur) {}
