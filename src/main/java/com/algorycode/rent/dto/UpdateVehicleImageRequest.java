package com.algorycode.rent.dto;

import jakarta.validation.constraints.NotBlank;

/** Tek slot için yeni görsel (data URL veya mevcut object key / URL). */
public record UpdateVehicleImageRequest(@NotBlank String image) {}
