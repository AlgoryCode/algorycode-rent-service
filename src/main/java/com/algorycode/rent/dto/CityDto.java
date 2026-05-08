package com.algorycode.rent.dto;

public record CityDto(
    Long id, String name, Long countryId, String countryCode, String countryName) {}
