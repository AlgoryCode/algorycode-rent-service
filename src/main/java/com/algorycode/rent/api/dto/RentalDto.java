package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.rental.RentalStatus;
import com.algorycode.rent.domain.rental.RentalCommissionFlow;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RentalDto(
    UUID id,
    UUID vehicleId,
    UUID userId,
    LocalDate startDate,
    LocalDate endDate,
    Instant createdAt,
    RentalStatus status,
    BigDecimal commissionAmount,
    RentalCommissionFlow commissionFlow,
    String commissionCompany,
    CustomerDto customer,
    List<AdditionalDriverDto> additionalDrivers,
    FeedbackDto feedback,
    List<RentalPhotoDto> photos,
    List<AccidentReportDto> accidentReports) {

  public record CustomerDto(
      String fullName,
      String nationalId,
      String passportNo,
      String phone,
      String email,
      LocalDate birthDate,
      String driverLicenseNo,
      String driverLicenseImageDataUrl,
      String passportImageDataUrl) {}

  public record AdditionalDriverDto(
      UUID id,
      String fullName,
      LocalDate birthDate,
      String driverLicenseNo,
      String passportNo,
      String driverLicenseImageDataUrl,
      String passportImageDataUrl) {}

  public record FeedbackDto(Instant at, String text) {}

  public record RentalPhotoDto(String id, String url, String caption) {}

  public record AccidentReportDto(
      UUID id, Instant at, String description, List<RentalPhotoDto> photos) {}
}
