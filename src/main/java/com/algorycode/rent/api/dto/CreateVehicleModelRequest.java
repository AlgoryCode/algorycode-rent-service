package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateVehicleModelRequest(
    @NotBlank @Size(max = 255) String name, @Min(0) @Max(9999) int sortOrder) {}
