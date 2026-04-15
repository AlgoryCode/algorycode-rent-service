package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record VehicleOptionDefinitionRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 4000) String description,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal price,
    @Size(max = 512) String icon,
    @NotNull Integer lineOrder,
    Boolean active) {}
