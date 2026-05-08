package com.algorycode.rent.dto;

import java.time.Instant;
import java.time.LocalDate;

public record CustomerResponse(
    Long id,
    Instant createdAt,
    Instant updatedAt,
    String fullName,
    String nationalId,
    String passportNo,
    String phone,
    String email,
    LocalDate birthDate,
    String driverLicenseNo,
    String driverLicenseImageUrl,
    String passportImageUrl) {}
