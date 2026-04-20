package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCountryRequest(
    @NotBlank
        @Size(min = 2, max = 5)
        @Pattern(
            regexp = "^[A-Za-z]{2,5}$",
            message = "Ülke kodu 2–5 harf olmalıdır (yalnızca A–Z).")
        String code,
    @NotBlank @Size(max = 128) String name,
    @NotBlank
        @Pattern(
            regexp = "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$",
            message = "Geçerli hex renk girin (#RGB veya #RRGGBB)")
        String colorCode) {}
