package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractAuditableLongEntity;
import com.algorycode.rent.entity.HandoverLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "vehicle_allowed_return_handovers",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_vehicle_allowed_return_vehicle_handover",
            columnNames = {"vehicle_id", "handover_location_id"}))
public class VehicleAllowedReturnHandover extends AbstractAuditableLongEntity {

  @Column(name = "vehicle_id", nullable = false)
  private Long vehicleId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false, insertable = false, updatable = false)
  private Vehicle vehicle;

  @Column(name = "handover_location_id", nullable = false)
  private Long handoverLocationId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "handover_location_id",
      nullable = false,
      insertable = false,
      updatable = false)
  private HandoverLocation handoverLocation;

  @Column(name = "line_order", nullable = false)
  private int lineOrder;

  @PrePersist
  @PreUpdate
  void syncAllowedReturnFks() {
    if (vehicle != null) {
      vehicleId = vehicle.getId();
    }
    if (handoverLocation != null) {
      handoverLocationId = handoverLocation.getId();
    }
  }
}
