package com.algorycode.rent.domain.request;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rental_request_priced_lines")
public class RentalRequestPricedLine extends AbstractAuditableLongEntity {

  @Column(name = "rental_request_id", nullable = false)
  private Long rentalRequestId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_request_id", nullable = false, insertable = false, updatable = false)
  private RentalRequest rentalRequest;

  @Enumerated(EnumType.STRING)
  @Column(name = "line_type", nullable = false, length = 40)
  private RentalRequestPricedLineType lineType;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private int quantity = 1;

  @Column(name = "unit_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitAmount = BigDecimal.ZERO;

  @Column(name = "line_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal lineAmount = BigDecimal.ZERO;

  @Column(nullable = false, length = 3)
  private String currency = "TRY";

  @Column(name = "line_order", nullable = false)
  private int lineOrder;

  @Column(name = "priced_at")
  private Instant pricedAt;

  @Column(name = "source_reservation_extra_template_id")
  private Long sourceReservationExtraTemplateId;

  @Column(name = "source_vehicle_option_definition_id")
  private Long sourceVehicleOptionDefinitionId;

  @Column(name = "return_handover_location_id")
  private Long returnHandoverLocationId;

  @Column(columnDefinition = "TEXT")
  private String metadata;

  @PrePersist
  @PreUpdate
  void syncRentalRequestPricedLineFk() {
    if (rentalRequest != null && rentalRequest.getId() != null) {
      rentalRequestId = rentalRequest.getId();
    }
  }
}
