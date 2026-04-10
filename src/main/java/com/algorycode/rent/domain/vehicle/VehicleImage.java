package com.algorycode.rent.domain.vehicle;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "vehicle_images",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id", "slot"}))
public class VehicleImage extends AbstractAuditableUuidEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private VehicleImageSlot slot;

  /** URL/object key veya fallback data URL */
  @Lob
  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String imageUrl;
}
