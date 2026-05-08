package com.algorycode.rent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCityRequest(@NotBlank @Size(max = 128) String name, @NotNull Long countryId) {}
