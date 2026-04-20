package com.algorycode.rent.api.dto;

import java.math.BigDecimal;

public record VehicleOptionDefinitionDto(
    Long id,
    String title,
    String description,
    BigDecimal price,
    String icon,
    int lineOrder,
    boolean active) {}
