package com.algorycode.rent.api.dto;

import com.algorycode.rent.domain.rental.RentalCommissionFlow;
import com.algorycode.rent.domain.rental.RentalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateRentalRequest(
    LocalDate startDate,
    LocalDate endDate,
    UUID pickupHandoverLocationId,
    UUID returnHandoverLocationId,
    /** Sıfır geçerlidir (kendi aracı, komisyonsuz kapatma vb.). */
    @DecimalMin(value = "0", inclusive = true) BigDecimal commissionAmount,
    RentalCommissionFlow commissionFlow,
    @Size(max = 255) String commissionCompany,
    RentalStatus status,
    @Valid CustomerBody customer,
    @Size(max = 100) List<@Valid RentalOptionRequest> options) {

  public record CustomerBody(
      @Size(max = 255) String fullName,
      @Size(max = 32) String nationalId,
      @Size(max = 32) String passportNo,
      @Size(max = 32) String phone,
      @Size(max = 255) String email,
      LocalDate birthDate,
      @Size(max = 64) String driverLicenseNo,
      /** data URL veya mevcut object URL — dolu gelirse object storage’a yüklenir / normalize edilir */
      String passportImageDataUrl,
      String driverLicenseImageDataUrl) {}
}
