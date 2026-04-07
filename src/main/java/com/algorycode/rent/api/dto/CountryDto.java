package com.algorycode.rent.api.dto;

import java.util.UUID;

public record CountryDto(UUID id, String code, String name, String colorCode) {}
