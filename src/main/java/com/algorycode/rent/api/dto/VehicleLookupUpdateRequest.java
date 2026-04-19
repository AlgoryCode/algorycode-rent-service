package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record VehicleLookupUpdateRequest(
    @Size(max = 128) String labelTr, @Min(0) @Max(9999) Integer sortOrder) {}
