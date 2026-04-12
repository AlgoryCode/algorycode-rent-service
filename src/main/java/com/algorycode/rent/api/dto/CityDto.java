package com.algorycode.rent.api.dto;

import java.util.UUID;

public record CityDto(
    UUID id,
    String name,
    UUID countryId,
    String countryCode,
    String countryName) {}
