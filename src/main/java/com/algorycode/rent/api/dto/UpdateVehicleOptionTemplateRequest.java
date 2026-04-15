package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateVehicleOptionTemplateRequest(
    @Size(max = 255) String title,
    @Size(max = 4000) String description,
    @DecimalMin(value = "0", inclusive = true) BigDecimal price,
    @Size(max = 512) String icon,
    Integer lineOrder,
    Boolean active) {}
