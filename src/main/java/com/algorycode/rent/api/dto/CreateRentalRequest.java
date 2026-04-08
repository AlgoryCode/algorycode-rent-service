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
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @NotNull @Valid CustomerBody customer,
    @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal commissionAmount,
    @NotNull RentalCommissionFlow commissionFlow,
    @Size(max = 255) String commissionCompany,
    List<@Valid AdditionalDriverBody> additionalDrivers,
    RentalStatus status) {

  public record CustomerBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotBlank @Size(max = 32) String nationalId,
      @NotBlank @Size(max = 32) String passportNo,
      @NotBlank @Size(max = 32) String phone,
      @Email @Size(max = 255) String email,
      LocalDate birthDate,
      @Size(max = 64) String driverLicenseNo,
      @Size(max = 6_000_000) String driverLicenseImageDataUrl,
      @Size(max = 6_000_000) String passportImageDataUrl) {}

  public record AdditionalDriverBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotNull LocalDate birthDate,
      @NotBlank @Size(max = 64) String driverLicenseNo,
      @NotBlank @Size(max = 64) String passportNo,
      @NotBlank @Size(max = 6_000_000) String driverLicenseImageDataUrl,
      @NotBlank @Size(max = 6_000_000) String passportImageDataUrl) {}
}
