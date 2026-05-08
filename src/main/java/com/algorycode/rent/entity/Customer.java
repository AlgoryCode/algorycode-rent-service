package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractAuditableLongEntity;
import com.algorycode.rent.entity.Rental;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
public class Customer extends AbstractAuditableLongEntity {

  @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
  @Builder.Default
  private List<Rental> rentals = new ArrayList<>();

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
