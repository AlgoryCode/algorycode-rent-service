package com.algorycode.rent.domain.rental;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

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
}
