package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.request.RentalRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RentalRequestDto(
    UUID id,
    String referenceNo,
    Instant createdAt,
    RentalRequestStatus status,
    String statusMessage,
    UUID vehicleId,
    UUID userId,
    LocalDate startDate,
    LocalDate endDate,
    HandoverLocationRefDto pickupHandoverLocation,
    HandoverLocationRefDto returnHandoverLocation,
    boolean outsideCountryTravel,
    BigDecimal greenInsuranceFee,
    String note,
    String contractPdfPath,
    Instant whatsappContractSentAt,
    String whatsappContractError,
    /**
     * {@code true} when status is approved and no contract PDF exists yet — show “generate contract” and then
     * {@code POST /rental-requests/{id}/contract} (triggers PDF + admin WhatsApp if configured).
     */
    boolean contractGenerationAvailable,
    CustomerDto customer,
    List<AdditionalDriverDto> additionalDrivers,
    List<RentalRequestOptionDto> options) {

  public record CustomerDto(
      String fullName,
      String phone,
      String email,
      LocalDate birthDate,
      String nationalId,
      String passportNo,
      String driverLicenseNo,
      String passportImageDataUrl,
      String driverLicenseImageDataUrl) {}

  public record AdditionalDriverDto(
      UUID id,
      String fullName,
      LocalDate birthDate,
      String driverLicenseNo,
      String passportNo,
      String passportImageDataUrl,
      String driverLicenseImageDataUrl) {}

  public record RentalRequestOptionDto(
      UUID id,
      String title,
      String description,
      BigDecimal price,
      String icon) {}
}
