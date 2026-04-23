package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

public record HandoverLocationDto(
    Long id,
    HandoverLocationKind kind,
    String name,
    String description,
    String addressLine,
    String countryCode,
    boolean active,
    int lineOrder,
    BigDecimal surchargeEur,
    /** user-fe hero / {@code feHandoverSnapshot} satırı. */
    JsonNode feHandoverSnapshot) {}
