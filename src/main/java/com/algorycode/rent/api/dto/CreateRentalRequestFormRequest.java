package com.algorycode.rent.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateRentalRequestFormRequest(
    UUID vehicleId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    boolean outsideCountryTravel,
    @Size(max = 1000) String note,
    @NotNull @Valid CustomerBody customer,
    List<@Valid AdditionalDriverBody> additionalDrivers) {

  public record CustomerBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotBlank @Size(max = 32) String phone,
      @NotBlank @Email @Size(max = 255) String email,
      @NotNull LocalDate birthDate,
      @Size(max = 32) String nationalId,
      @NotBlank @Size(max = 64) String passportNo,
      @NotBlank @Size(max = 64) String driverLicenseNo,
      @NotBlank @Size(max = 6_000_000) String passportImageDataUrl,
      @NotBlank @Size(max = 6_000_000) String driverLicenseImageDataUrl) {}

  public record AdditionalDriverBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotNull LocalDate birthDate,
      @NotBlank @Size(max = 64) String driverLicenseNo,
      @NotBlank @Size(max = 64) String passportNo,
      @NotBlank @Size(max = 6_000_000) String passportImageDataUrl,
      @NotBlank @Size(max = 6_000_000) String driverLicenseImageDataUrl) {}
}
