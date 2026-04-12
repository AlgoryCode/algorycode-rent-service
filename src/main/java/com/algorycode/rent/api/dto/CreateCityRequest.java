package com.algorycode.rent.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCityRequest(
    @NotBlank @Size(max = 128) String name,
    @NotNull UUID countryId) {}
