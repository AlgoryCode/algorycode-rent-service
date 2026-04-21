package com.algorycode.rent.domain.rental;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Embeddable
public class CustomerSnapshot {

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "national_id", nullable = false, length = 32)
  private String nationalId;

  @Column(name = "passport_no", nullable = false, length = 32)
  private String passportNo;

  @Column(name = "phone", nullable = false, length = 32)
  private String phone;

  @Column(name = "email", length = 255)
  private String email;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "driver_license_no", length = 64)
  private String driverLicenseNo;

  @Lob
  @Column(name = "driver_license_image_data_url", columnDefinition = "TEXT")
  private String driverLicenseImageDataUrl;

  @Lob
  @Column(name = "passport_image_data_url", columnDefinition = "TEXT")
  private String passportImageDataUrl;
}
