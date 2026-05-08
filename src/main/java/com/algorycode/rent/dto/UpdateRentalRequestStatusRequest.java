package com.algorycode.rent.dto;

import com.algorycode.rent.entity.RentalRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRentalRequestStatusRequest(
    @NotNull RentalRequestStatus status, @Size(max = 500) String statusMessage) {}
