package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.request.RentalRequestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record RentalRequestDto(
    Long id,
    String referenceNo,
    Instant createdAt,
    RentalRequestStatus status,
    String statusMessage,
    Long vehicleId,
    UUID userId,
    LocalDate startDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime returnTime,
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
    BigDecimal handoverPickupLegEur,
    BigDecimal handoverReturnLegEur,
    BigDecimal handoverRouteEur,
    BigDecimal handoverTotalEur,
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
      Long id,
      String fullName,
      LocalDate birthDate,
      String driverLicenseNo,
      String passportNo,
      String passportImageDataUrl,
      String driverLicenseImageDataUrl) {}

  public record RentalRequestOptionDto(
      Long id,
      String title,
      String description,
      BigDecimal price,
      String icon) {}
}
