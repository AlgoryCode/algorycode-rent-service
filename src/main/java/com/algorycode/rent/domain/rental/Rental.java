package com.algorycode.rent.domain.rental;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.vehicle.Vehicle;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Getter
@Setter
@Entity
@Table(name = "rentals")
public class Rental extends AbstractAuditableLongEntity {

  @Column(name = "vehicle_id", nullable = false)
  private Long vehicleId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false, insertable = false, updatable = false)
  private Vehicle vehicle;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @Column(name = "pickup_handover_location_id")
  private Long pickupHandoverLocationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pickup_handover_location_id", insertable = false, updatable = false)
  private HandoverLocation pickupHandoverLocation;

  @Column(name = "return_handover_location_id")
  private Long returnHandoverLocationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "return_handover_location_id", insertable = false, updatable = false)
  private HandoverLocation returnHandoverLocation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "rental_status_id", nullable = false)
  private RentalStatusDefinition statusDefinition;

  @Embedded private CustomerSnapshot customer;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal commissionAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(name = "commission_flow", nullable = false, length = 16)
  private RentalCommissionFlow commissionFlow = RentalCommissionFlow.collect;

  @Column(name = "commission_company", length = 255)
  private String commissionCompany;

  @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "discount_type", length = 16)
  private String discountType;

  @Column(name = "net_amount", precision = 12, scale = 2)
  private BigDecimal netAmount;

  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 32)
  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RentalAdditionalDriver> additionalDrivers = new ArrayList<>();

  @OneToOne(
      mappedBy = "rental",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private RentalFeedback feedback;

  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 32)
  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RentalPhoto> photos = new ArrayList<>();

  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 32)
  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AccidentReport> accidentReports = new ArrayList<>();

  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 32)
  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC")
  private List<RentalOption> options = new ArrayList<>();

  public RentalStatus getStatus() {
    if (statusDefinition == null) {
      return RentalStatus.active;
    }
    return RentalStatus.fromDbCode(statusDefinition.getCode());
  }

  @PrePersist
  @PreUpdate
  void syncRentalFks() {
    if (vehicle != null && vehicle.getId() != null) {
      vehicleId = vehicle.getId();
    }
    if (pickupHandoverLocation != null && pickupHandoverLocation.getId() != null) {
      pickupHandoverLocationId = pickupHandoverLocation.getId();
    }
    if (returnHandoverLocation != null && returnHandoverLocation.getId() != null) {
      returnHandoverLocationId = returnHandoverLocation.getId();
    }
  }
}
