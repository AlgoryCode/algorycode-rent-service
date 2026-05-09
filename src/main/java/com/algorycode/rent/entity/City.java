package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractAuditableLongEntity;
import com.algorycode.rent.entity.Country;
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

@Getter
@Setter
@Entity
@Table(name = "cities")
public class City extends AbstractAuditableLongEntity {

  @Column(nullable = false, length = 128)
  private String name;

  @Column(name = "country_id", nullable = false)
  private Long countryId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "country_id", nullable = false, insertable = false, updatable = false)
  private Country country;

  @PrePersist
  @PreUpdate
  void syncCountryFk() {
    if (country != null) {
      countryId = country.getId();
    }
  }
}
