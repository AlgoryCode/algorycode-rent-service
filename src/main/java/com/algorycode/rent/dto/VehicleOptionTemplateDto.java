package com.algorycode.rent.dto;

import java.math.BigDecimal;

public record VehicleOptionTemplateDto(
    Long id,
    String title,
    String description,
    BigDecimal price,
    String icon,
    int lineOrder,
    boolean active) {}
