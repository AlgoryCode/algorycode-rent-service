package com.algorycode.rent.domain.request;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "rental_request_additional_drivers")
public class RentalRequestAdditionalDriver extends AbstractAuditableLongEntity {

  @Column(name = "rental_request_id", nullable = false)
  private Long rentalRequestId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_request_id", nullable = false, insertable = false, updatable = false)
  private RentalRequest rentalRequest;

  @Column(name = "full_name", nullable = false, length = 255)
  private String fullName;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "driver_license_no", nullable = false, length = 64)
  private String driverLicenseNo;

  @Column(name = "passport_no", nullable = false, length = 64)
  private String passportNo;

  @Column(name = "driver_license_image_data_url", nullable = false, columnDefinition = "TEXT")
  private String driverLicenseImageDataUrl;

  @Column(name = "passport_image_data_url", nullable = false, columnDefinition = "TEXT")
  private String passportImageDataUrl;

  @PrePersist
  @PreUpdate
  void syncRentalRequestAdditionalDriverFk() {
    if (rentalRequest != null && rentalRequest.getId() != null) {
      rentalRequestId = rentalRequest.getId();
    }
  }
}
