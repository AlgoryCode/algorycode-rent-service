package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateRentalRequest(
    @NotNull UUID vehicleId,
    UUID userId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    UUID pickupHandoverLocationId,
    UUID returnHandoverLocationId,
    @NotNull @Valid CustomerBody customer,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal commissionAmount,
    @NotNull RentalCommissionFlow commissionFlow,
    @Size(max = 255) String commissionCompany,
    @Size(max = 1)
    List<@Valid AdditionalDriverBody> additionalDrivers,
    RentalStatus status,
    @Size(max = 100) List<@Valid RentalOptionRequest> options) {

  public record CustomerBody(
      @NotBlank @Size(max = 255) String fullName,
      @Size(max = 32) String nationalId,
      @Size(max = 32) String passportNo,
      @NotBlank @Size(max = 32) String phone,
      @Email @Size(max = 255) String email,
      LocalDate birthDate,
      @Size(max = 64) String driverLicenseNo,
      @Size(max = 6_000_000) String driverLicenseImageDataUrl,
      @Size(max = 6_000_000) String passportImageDataUrl) {}

  public record AdditionalDriverBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotNull LocalDate birthDate,
      @Size(max = 64) String driverLicenseNo,
      @Size(max = 64) String passportNo,
      @NotBlank @Size(max = 6_000_000) String driverLicenseImageDataUrl,
      @NotBlank @Size(max = 6_000_000) String passportImageDataUrl) {}
}
