package com.algorycode.rent.api.dto;

public record CityDto(
    Long id, String name, Long countryId, String countryCode, String countryName) {}
