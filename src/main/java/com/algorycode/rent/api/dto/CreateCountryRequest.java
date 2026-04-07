package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCountryRequest(
    @NotBlank
        @Size(min = 2, max = 2)
        @Pattern(
            regexp = "^[A-Za-z]{2}$",
            message = "Ülke kodu tam 2 harf olmalıdır (ISO 3166-1 alpha-2)")
        String code,
    @NotBlank @Size(max = 128) String name,
    @NotBlank
        @Pattern(
            regexp = "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$",
            message = "Geçerli hex renk girin (#RGB veya #RRGGBB)")
        String colorCode) {}
