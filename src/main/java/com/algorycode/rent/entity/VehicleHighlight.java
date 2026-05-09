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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vehicle_highlights")
public class VehicleHighlight extends AbstractAuditableLongEntity {

  @Column(name = "vehicle_id", nullable = false)
  private Long vehicleId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false, insertable = false, updatable = false)
  private Vehicle vehicle;

  @Column(name = "line_order", nullable = false)
  private int lineOrder;

  @Column(nullable = false, length = 500)
  private String text;

  @PrePersist
  @PreUpdate
  void syncVehicleHighlightFk() {
    if (vehicle != null) {
      vehicleId = vehicle.getId();
    }
  }
}
