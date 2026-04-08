package com.algorycode.rent.domain.request;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Embeddable
public class RentalRequestCustomerSnapshot {

  @Column(name = "full_name", nullable = false, length = 255)
  private String fullName;

  @Column(name = "phone", nullable = false, length = 32)
  private String phone;

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "national_id", length = 32)
  private String nationalId;

  @Column(name = "passport_no", nullable = false, length = 64)
  private String passportNo;

  @Column(name = "driver_license_no", nullable = false, length = 64)
  private String driverLicenseNo;

  @Lob
  @Column(name = "passport_image_data_url", nullable = false, columnDefinition = "LONGTEXT")
  private String passportImageDataUrl;

  @Lob
  @Column(name = "driver_license_image_data_url", nullable = false, columnDefinition = "LONGTEXT")
  private String driverLicenseImageDataUrl;
}
