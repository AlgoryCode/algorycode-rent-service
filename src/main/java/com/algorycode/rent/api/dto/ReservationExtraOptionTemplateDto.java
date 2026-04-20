package com.algorycode.rent.api.dto;

import java.math.BigDecimal;

public record ReservationExtraOptionTemplateDto(
    Long id,
    String code,
    String title,
    String description,
    BigDecimal price,
    String icon,
    int lineOrder,
    boolean active,
    boolean requiresCoDriverDetails) {}
