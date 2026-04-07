package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateVehicleRequest(
    @NotBlank @Size(max = 32) String plate,
    @NotBlank @Size(max = 255) String brand,
    @NotBlank @Size(max = 255) String model,
    @NotNull @Min(1950) @Max(2100) Integer year,
    boolean maintenance,
    /** ISO 3166-1 alpha-2; kayıtlı ülke kodu veya boş. */
    @Size(min = 2, max = 2) String countryCode,
    Map<String, String> images) {}
