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
@Table(name = "rental_options")
public class RentalOption extends AbstractAuditableLongEntity {

  @Column(name = "rental_id", nullable = false)
  private Long rentalId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_id", nullable = false, insertable = false, updatable = false)
  private Rental rental;

  @Column(name = "vehicle_option_definition_id")
  private Long vehicleOptionDefinitionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "vehicle_option_definition_id",
      insertable = false,
      updatable = false)
  private VehicleOptionDefinition vehicleOptionDefinition;

  @Column(name = "reservation_extra_template_id")
  private Long reservationExtraTemplateId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "reservation_extra_template_id",
      insertable = false,
      updatable = false)
  private ReservationExtraOptionTemplate reservationExtraTemplate;

  @Column(name = "line_order", nullable = false)
  private int lineOrder;

  @PrePersist
  @PreUpdate
  void syncRentalOptionFk() {
    if (rental != null) {
      rentalId = rental.getId();
    }
    if (vehicleOptionDefinition != null) {
      vehicleOptionDefinitionId = vehicleOptionDefinition.getId();
    }
    if (reservationExtraTemplate != null) {
      reservationExtraTemplateId = reservationExtraTemplate.getId();
    }
  }
}
