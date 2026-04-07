package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.rental.RentalStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RentalDto(
    UUID id,
    UUID vehicleId,
    LocalDate startDate,
    LocalDate endDate,
    Instant createdAt,
    RentalStatus status,
    CustomerDto customer,
    FeedbackDto feedback,
    List<RentalPhotoDto> photos,
    List<AccidentReportDto> accidentReports) {

  public record CustomerDto(
      String fullName, String nationalId, String passportNo, String phone) {}

  public record FeedbackDto(Instant at, String text) {}

  public record RentalPhotoDto(String id, String url, String caption) {}

  public record AccidentReportDto(
      UUID id, Instant at, String description, List<RentalPhotoDto> photos) {}
}
