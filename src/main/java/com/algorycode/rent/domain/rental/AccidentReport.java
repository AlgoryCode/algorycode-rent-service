package com.algorycode.rent.domain.rental;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "accident_reports")
public class AccidentReport extends AbstractAuditableUuidEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_id", nullable = false)
  private Rental rental;

  @Column(nullable = false)
  private Instant at;

  @Column(nullable = false, length = 4000)
  private String description;

  @OneToMany(mappedBy = "accidentReport", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AccidentPhoto> photos = new ArrayList<>();
}
