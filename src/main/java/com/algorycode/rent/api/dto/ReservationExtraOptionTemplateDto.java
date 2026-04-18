package com.algorycode.rent.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationExtraOptionTemplateDto(
    UUID id,
    String code,
    String title,
    String description,
    BigDecimal price,
    String icon,
    int lineOrder,
    boolean active,
    boolean requiresCoDriverDetails) {}
