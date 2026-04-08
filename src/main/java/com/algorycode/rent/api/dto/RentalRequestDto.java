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
    LocalDate startDate,
    LocalDate endDate,
    boolean outsideCountryTravel,
    BigDecimal greenInsuranceFee,
    String note,
    String contractPdfPath,
    Instant whatsappContractSentAt,
    String whatsappContractError,
    CustomerDto customer,
    List<AdditionalDriverDto> additionalDrivers) {

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
}
