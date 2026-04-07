package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.rental.RentalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRentalRequest(
    @NotNull UUID vehicleId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotNull @Valid CustomerBody customer,
    RentalStatus status) {

  public record CustomerBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotBlank @Size(max = 32) String nationalId,
      @NotBlank @Size(max = 32) String passportNo,
      @NotBlank @Size(max = 32) String phone) {}
}
