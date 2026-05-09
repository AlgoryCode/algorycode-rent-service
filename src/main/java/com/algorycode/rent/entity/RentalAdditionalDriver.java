package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rental_additional_drivers")
public class RentalAdditionalDriver extends AbstractAuditableLongEntity {

  @Column(name = "rental_id", nullable = false)
  private Long rentalId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_id", nullable = false, insertable = false, updatable = false)
  private Rental rental;

  @Column(name = "full_name", nullable = false, length = 255)
  private String fullName;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "driver_license_no", nullable = false, length = 64)
  private String driverLicenseNo;

  @Column(name = "passport_no", nullable = false, length = 64)
  private String passportNo;

  @Column(name = "driver_license_image_data_url", columnDefinition = "TEXT")
  private String driverLicenseImageDataUrl;

  @Column(name = "passport_image_data_url", columnDefinition = "TEXT")
  private String passportImageDataUrl;

  @PrePersist
  @PreUpdate
  void syncRentalAdditionalDriverFk() {
    if (rental != null) {
      rentalId = rental.getId();
    }
  }
}
