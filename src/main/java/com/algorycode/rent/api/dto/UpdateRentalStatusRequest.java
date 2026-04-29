package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.rental.RentalStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRentalStatusRequest(@NotNull RentalStatus status) {}
