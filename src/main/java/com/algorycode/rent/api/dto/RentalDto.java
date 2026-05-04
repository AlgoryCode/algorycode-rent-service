package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import com.algorycode.rent.domain.rental.RentalStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RentalDto(
    Long id,
    Long vehicleId,
    UUID userId,
    LocalDate startDate,
    LocalDate endDate,
    HandoverLocationRefDto pickupHandoverLocation,
    HandoverLocationRefDto returnHandoverLocation,
    Instant createdAt,
    RentalStatus status,
    String statusCode,
    BigDecimal commissionAmount,
    RentalCommissionFlow commissionFlow,
    String commissionCompany,
    BigDecimal discountAmount,
    String discountType,
    BigDecimal netAmount,
    CustomerDto customer,
    List<AdditionalDriverDto> additionalDrivers,
    FeedbackDto feedback,
    List<RentalPhotoDto> photos,
    List<AccidentReportDto> accidentReports,
    List<RentalOptionDto> options) {

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
      Long id,
      String fullName,
      LocalDate birthDate,
      String driverLicenseNo,
      String passportNo,
      String driverLicenseImageDataUrl,
      String passportImageDataUrl) {}

  public record FeedbackDto(Instant at, String text) {}

  public record RentalPhotoDto(Long id, String url, String caption) {}

  public record AccidentReportDto(
      Long id, Instant at, String description, List<RentalPhotoDto> photos) {}

  public record RentalOptionDto(
      Long id,
      String title,
      String description,
      BigDecimal price,
      /** İkon; null olabilir. */
      String icon) {}
}
