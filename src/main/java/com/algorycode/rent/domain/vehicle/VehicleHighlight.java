package com.algorycode.rent.domain.vehicle;

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
@Table(name = "vehicle_highlights")
public class VehicleHighlight extends AbstractAuditableUuidEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  @Column(name = "line_order", nullable = false)
  private int lineOrder;

  @Column(nullable = false, length = 500)
  private String text;
}
