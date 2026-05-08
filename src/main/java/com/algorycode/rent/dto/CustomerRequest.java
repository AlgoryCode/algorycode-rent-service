package com.algorycode.rent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CustomerRequest(
    @NotBlank @Size(max = 255) String fullName,
    @Size(max = 32) String nationalId,
    @Size(max = 32) String passportNo,
    @NotBlank @Size(max = 32) String phone,
    @NotBlank @Email @Size(max = 255) String email,
    LocalDate birthDate,
    @Size(max = 64) String driverLicenseNo,
    @Size(max = 67_000_000) String driverLicenseImageDataUrl,
    @Size(max = 67_000_000) String passportImageDataUrl) {}
