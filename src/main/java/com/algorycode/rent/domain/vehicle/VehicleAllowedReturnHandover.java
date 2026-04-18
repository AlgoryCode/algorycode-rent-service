package com.algorycode.rent.domain.vehicle;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import com.algorycode.rent.domain.location.HandoverLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class VehicleAllowedReturnHandover extends AbstractAuditableUuidEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "handover_location_id", nullable = false)
  private HandoverLocation handoverLocation;

  @Column(name = "line_order", nullable = false)
  private int lineOrder;
}
