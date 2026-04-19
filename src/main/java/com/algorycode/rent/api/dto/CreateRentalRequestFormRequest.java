package com.algorycode.rent.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateRentalRequestFormRequest(
    UUID vehicleId,
    UUID userId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    /** Bos ise PDF ve kayitta 08:00 kullanilir. */
    LocalTime startTime,
    /** Bos ise PDF ve kayitta 08:00 kullanilir. */
    LocalTime returnTime,
    UUID pickupHandoverLocationId,
    UUID returnHandoverLocationId,
    boolean outsideCountryTravel,
    @Size(max = 1000) String note,
    @NotNull @Valid CustomerBody customer,
    @Size(max = 1)
    List<@Valid AdditionalDriverBody> additionalDrivers,
    @Size(max = 100) List<@Valid RentalOptionRequest> options) {

  public record CustomerBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotBlank @Size(max = 32) String phone,
      @NotBlank @Email @Size(max = 255) String email,
      @NotNull LocalDate birthDate,
      @Size(max = 32) String nationalId,
      @Size(max = 64) String passportNo,
      @Size(max = 64) String driverLicenseNo,
      @NotBlank @Size(max = 6_000_000) String passportImageDataUrl,
      @NotBlank @Size(max = 6_000_000) String driverLicenseImageDataUrl) {}

  public record AdditionalDriverBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotNull LocalDate birthDate,
      @Size(max = 64) String driverLicenseNo,
      @Size(max = 64) String passportNo,
      @NotBlank @Size(max = 6_000_000) String passportImageDataUrl,
      @NotBlank @Size(max = 6_000_000) String driverLicenseImageDataUrl) {}
}
