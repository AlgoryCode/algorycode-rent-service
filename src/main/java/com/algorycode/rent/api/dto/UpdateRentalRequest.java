package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import com.algorycode.rent.domain.rental.RentalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateRentalRequest(
    LocalDate startDate,
    LocalDate endDate,
    @DecimalMin(value = "0.01", inclusive = true) BigDecimal commissionAmount,
    RentalCommissionFlow commissionFlow,
    @Size(max = 255) String commissionCompany,
    RentalStatus status,
    @Valid CustomerBody customer) {

  public record CustomerBody(
      @Size(max = 255) String fullName,
      @Size(max = 32) String nationalId,
      @Size(max = 32) String passportNo,
      @Size(max = 32) String phone,
      @Email @Size(max = 255) String email,
      LocalDate birthDate,
      @Size(max = 64) String driverLicenseNo) {}
}
