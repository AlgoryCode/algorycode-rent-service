package com.algorycode.rent.api.dto;

import java.util.Map;
import java.util.UUID;

/** AlgoryRent FE {@code Vehicle} ile uyumlu alanlar. */
public record VehicleDto(
    UUID id,
    String plate,
    String brand,
    String model,
    int year,
    boolean maintenance,
    String countryCode,
    Map<String, String> images) {}
