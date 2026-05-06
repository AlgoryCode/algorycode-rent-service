package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record VehicleOptionDefinitionRequest(
    @Size(max = 255) String title,
    @Size(max = 4000) String description,
    BigDecimal price,
    @Size(max = 512) String icon,
    Integer lineOrder,
    Boolean active) {}
