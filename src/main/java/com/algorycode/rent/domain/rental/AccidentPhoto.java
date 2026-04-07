package com.algorycode.rent.domain.rental;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
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
@Table(name = "accident_photos")
public class AccidentPhoto extends AbstractAuditableUuidEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "accident_report_id", nullable = false)
  private AccidentReport accidentReport;

  @Column(nullable = false, length = 4096)
  private String url;

  @Column(length = 512)
  private String caption;
}
