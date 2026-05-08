package com.algorycode.rent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRentalStatusRequest(@NotBlank @Size(max = 64) String status) {}
