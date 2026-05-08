package com.algorycode.rent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCountryColorRequest(
    @NotBlank
        @Pattern(
            regexp = "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$",
            message = "Geçerli hex renk girin (#RGB veya #RRGGBB)")
        String colorCode) {}
