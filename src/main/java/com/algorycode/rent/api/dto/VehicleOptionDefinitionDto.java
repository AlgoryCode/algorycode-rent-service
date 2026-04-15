package com.algorycode.rent.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record VehicleOptionDefinitionDto(
    UUID id,
    String title,
    String description,
    BigDecimal price,
    String icon,
    int lineOrder,
    boolean active) {}
