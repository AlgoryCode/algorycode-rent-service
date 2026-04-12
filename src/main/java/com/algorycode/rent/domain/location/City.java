package com.algorycode.rent.domain.location;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import com.algorycode.rent.domain.country.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cities")
public class City extends AbstractAuditableUuidEntity {

  @Column(nullable = false, length = 128)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "country_id", nullable = false)
  private Country country;
}
