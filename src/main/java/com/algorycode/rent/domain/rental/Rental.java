package com.algorycode.rent.domain.rental;

import com.algorycode.rent.domain.AbstractAuditableUuidEntity;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "rentals")
public class Rental extends AbstractAuditableUuidEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vehicle_id", nullable = false)
  private Vehicle vehicle;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pickup_handover_location_id")
  private HandoverLocation pickupHandoverLocation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "return_handover_location_id")
  private HandoverLocation returnHandoverLocation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private RentalStatus status = RentalStatus.active;

  @Embedded
  private CustomerSnapshot customer;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal commissionAmount = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(name = "commission_flow", nullable = false, length = 16)
  private RentalCommissionFlow commissionFlow = RentalCommissionFlow.collect;

  @Column(name = "commission_company", length = 255)
  private String commissionCompany;

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RentalAdditionalDriver> additionalDrivers = new ArrayList<>();

  @OneToOne(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private RentalFeedback feedback;

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RentalPhoto> photos = new ArrayList<>();

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AccidentReport> accidentReports = new ArrayList<>();

  @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("lineOrder ASC")
  private List<RentalOption> options = new ArrayList<>();
}
