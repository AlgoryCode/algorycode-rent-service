package com.algorycode.rent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateRentalRequest(
    @NotNull Long vehicleId,
    UUID userId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    Long pickupHandoverLocationId,
    Long returnHandoverLocationId,
    @NotNull Long customerId,
    @Size(max = 1) List<@Valid AdditionalDriverBody> additionalDrivers,
    @Size(max = 64) String status,
    @Size(max = 100) List<@NotNull Long> vehicleOptionDefinitionIds,
    @Size(max = 100) List<@NotNull Long> reservationExtraTemplateIds) {

  public record AdditionalDriverBody(
      @NotBlank @Size(max = 255) String fullName,
      @NotNull LocalDate birthDate,
      @Size(max = 64) String driverLicenseNo,
      @Size(max = 64) String passportNo,
      @NotBlank @Size(max = 67_000_000) String driverLicenseImageDataUrl,
      @NotBlank @Size(max = 67_000_000) String passportImageDataUrl) {}
}
